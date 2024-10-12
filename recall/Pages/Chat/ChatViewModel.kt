package com.example.recall.Pages.Chat


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.recall.R
import com.example.recall.model.Message
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(@ApplicationContext val context: Context) : ViewModel() {


    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val message = _messages.asStateFlow()
    private val db = Firebase.database

    fun sendMessage(channelID: String, messageText: String?, image: String? = null) {
        val message = Message(
            db.reference.push().key ?: UUID.randomUUID().toString(),
            Firebase.auth.currentUser?.uid ?: "",
            messageText,
            System.currentTimeMillis(),
            Firebase.auth.currentUser?.displayName ?: "",
            null,
            image
        )

        db.reference.child("messages").child(channelID).push().setValue(message)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    postNotificationToUsers(channelID, message.senderName, messageText ?: "")
                }
            }
    }

    fun sendImageMessage(uri: Uri, channelID: String) {
        val imageRef = Firebase.storage.reference.child("images/${UUID.randomUUID()}")
        imageRef.putFile(uri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            val currentUser = Firebase.auth.currentUser
            if (task.isSuccessful) {
                val downloadUri = task.result
                sendMessage(channelID, null, downloadUri.toString())
            }
        }
    }

    fun listenForMessages(channelID: String) {
        db.getReference("messages").child(channelID).orderByChild("createdAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<com.example.recall.model.Message>()
                    snapshot.children.forEach { data ->
                        val message = data.getValue(Message::class.java)
                        message?.let {
                            list.add(it)
                        }
                    }
                    _messages.value = list
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        subscribeForNotification(channelID)
        registerUserIdtoChannel(channelID)
    }

    fun getAllUserEmails(channelID: String, callback: (List<String>) -> Unit) {
        val ref = db.reference.child("channels").child(channelID).child("users")
        val userIds = mutableListOf<String>()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    userIds.add(it.value.toString())
                }
                callback.invoke(userIds)
            }

            override fun onCancelled(error: DatabaseError) {
                callback.invoke(emptyList())
            }
        })
    }

    fun registerUserIdtoChannel(channelID: String) {
        val currentUser = Firebase.auth.currentUser
        val ref = db.reference.child("channels").child(channelID).child("users")
        ref.child(currentUser?.uid ?: "").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        ref.child(currentUser?.uid ?: "").setValue(currentUser?.email)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
        )

    }

    private fun subscribeForNotification(channelID: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("group_$channelID")
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("ChatViewModel", "Subscribed to topic: group_$channelID")
                } else {
                    Log.d("ChatViewModel", "Failed to subscribe to topic: group_$channelID")
                    // Handle failure
                }
            }
    }

    private fun postNotificationToUsers(
        channelID: String,
        senderName: String,
        messageContent: String
    ) {
        val fcmUrl = "https://fcm.googleapis.com/v1/projects/recall-7f8c9/messages:send"
        val jsonBody = JSONObject().apply {
            put("message", JSONObject().apply {
                put("topic", "group_$channelID")
                put("notification", JSONObject().apply {
                    put("title", "New message in $channelID")
                    put("body", "$senderName: $messageContent")
                })
            })
        }

        val requestBody = jsonBody.toString()

        val request = object : StringRequest(Method.POST, fcmUrl, Response.Listener {
            Log.d("ChatViewModel", "Notification sent successfully")
        }, Response.ErrorListener {
            Log.e("ChatViewModel", "Failed to send notification")
        }) {
            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${getAccessToken()}"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }

    private fun getAccessToken(): String {
        val inputStream = context.resources.openRawResource(R.raw.recall_key)
        val googleCreds = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        return googleCreds.refreshAccessToken().tokenValue
    }


    fun reactToMessage(channelID: String, messageID: String, reaction: String) {
        val messageRef = db.reference.child("messages").child(channelID).child(messageID)
        val currentUserId = Firebase.auth.currentUser?.uid ?: return

        messageRef.child("reactions").runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val reactions = currentData.getValue(object : GenericTypeIndicator<MutableMap<String, MutableList<String>>>() {}) ?: mutableMapOf()

                if (reactions[reaction] == null) {
                    reactions[reaction] = mutableListOf(currentUserId)
                } else {
                    val users = reactions[reaction]!!
                    if (currentUserId in users) {
                        users.remove(currentUserId)
                        if (users.isEmpty()) {
                            reactions.remove(reaction)
                        }
                    } else {
                        users.add(currentUserId)
                    }
                }

                currentData.value = reactions
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (committed) {
                    Log.d("ChatViewModel", "Reaction updated successfully")
                    // Update the local message list
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == messageID) {
                            msg.copy(reactions = currentData?.getValue(object : GenericTypeIndicator<Map<String, List<String>>>() {}) ?: emptyMap())
                        } else {
                            msg
                        }
                    }
                } else {
                    Log.e("ChatViewModel", "Failed to update reaction", error?.toException())
                }
            }
        })
    }




}