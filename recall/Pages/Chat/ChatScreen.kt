package com.example.recall.Pages.Chat

import android.Manifest
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.recall.Pages.Home.ChannelItem
import com.example.recall.R
import com.example.recall.model.Message
import com.example.recall.ui.theme.TextHighlight1
import com.example.recall.ui.theme.background_dark
import com.example.recall.ui.theme.bubbleColorOne
import com.example.recall.ui.theme.bubbleColorTwo
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, channelId: String, channelName: String) {
    Scaffold(
        containerColor = background_dark,
    ) {
        val viewModel: ChatViewModel = hiltViewModel()
        val chooserDialog = remember { mutableStateOf(false) }
        val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

        // State for managing the current message and its reactions
        rememberCoroutineScope()


        val cameraImageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                cameraImageUri.value?.let {
                    viewModel.sendImageMessage(it, channelId)
                }
            }
        }

        val imageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { viewModel.sendImageMessage(it, channelId) }
        }

        fun createImageUri(): Uri {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = ContextCompat.getExternalFilesDirs(
                navController.context, Environment.DIRECTORY_PICTURES
            ).first()
            return FileProvider.getUriForFile(navController.context,
                "${navController.context.packageName}.provider",
                File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
                    cameraImageUri.value = Uri.fromFile(this)
                })
        }

        val permissionLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    cameraImageLauncher.launch(createImageUri())
                }
            }


        Column(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            LaunchedEffect(key1 = true) {
                viewModel.listenForMessages(channelId)
            }
            val messages = viewModel.message.collectAsState()
            ChatMessages(
                messages = messages.value,
                onSendMessage = { message ->
                    viewModel.sendMessage(channelId, message)
                },
                onImageClicked = {
                    chooserDialog.value = true
                },
                channelName = channelName,
                viewModel = viewModel,
                channelID = channelId,
                onReactionClicked = { messageId, react ->
                    viewModel.reactToMessage(channelId, messageId, react)
                },
                navController = navController
            )
        }

        if (chooserDialog.value) {
            ContentSelectionDialog(onCameraSelected = {
                chooserDialog.value = false
                if (navController.context.checkSelfPermission(Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    cameraImageLauncher.launch(createImageUri())
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }, onGallerySelected = {
                chooserDialog.value = false
                imageLauncher.launch("image/*")
            })
        }
    }
}


@Composable
fun ContentSelectionDialog(onCameraSelected: () -> Unit, onGallerySelected: () -> Unit) {
    AlertDialog(onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onCameraSelected) {
                Text(text = "Camera")
            }
        },
        dismissButton = {
            TextButton(onClick = onGallerySelected) {
                Text(text = "Gallery")
            }
        },
        title = { Text(text = "Select your source?") },
        text = { Text(text = "Would you like to pick an image from the gallery or use the") })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMessages(
    channelName: String,
    channelID: String,
    messages: List<Message>,
    onSendMessage: (String) -> Unit,
    onImageClicked: () -> Unit,
    viewModel: ChatViewModel,
    onReactionClicked: (String, String) -> Unit,
    navController: NavController,
) {

    val hideKeyboardController = LocalSoftwareKeyboardController.current
    val msg = remember { mutableStateOf("") }
    val showPicker  = remember { mutableStateOf(false) }
    val selectedMessage = remember { mutableStateOf<Message?>(null) }



    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {


            item {
                ChannelItem(
                    channelName = channelName,
                    Modifier,
                    shouldShowCallButtons = true,
                    backButtonEnabled = true,
                    onClick = {},
                    onCall = { callButton ->
                        viewModel.getAllUserEmails(channelID) {
                            val list: MutableList<ZegoUIKitUser> = mutableListOf()
                            it.forEach{ email ->
                                Firebase.auth.currentUser?.email?.let { em ->
                                    if(email != em) {
                                        list.add(
                                            ZegoUIKitUser(email, email)
                                        )
                                    }
                                }

                            }
                            callButton.setInvitees (list)
                        }

                    },
                    onBackClick = {
                       navController.navigateUp()
                    }

                )
            }


            items(messages) { message ->
                val showReactions = remember { mutableStateOf(false) }
                ChatBubble(message = message, onChatBubbleClick = {
                showReactions.value = !showReactions.value
                    Timber.tag("ChatMessages").d("showReactions: ${showReactions.value}")
            },
                    onReactionClicked = onReactionClicked

                )
                if(showReactions.value){
                   ReactionPicker (
                       onReact = { react ->
                           onReactionClicked(message.id, react)
                           showReactions.value = false
                       }
                   )
                }

            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                msg.value = ""
                onImageClicked()
            }) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "send",
                    tint = TextHighlight1
                )
            }

            TextField(
                value = msg.value,
                onValueChange = { msg.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Type a message") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    hideKeyboardController?.hide()
                }),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = Color.DarkGray.copy(0.5f),
                    unfocusedContainerColor = Color.DarkGray.copy(0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedPlaceholderColor = Color.White,
                    unfocusedPlaceholderColor = Color.White
                )
            )
            IconButton(onClick = {
                onSendMessage(msg.value)
                msg.value = ""
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "send")
            }
        }
    }

    // Show reaction picker if user clicks on the bubble
   if(showPicker.value){
       selectedMessage.value?.let { msg ->
           ReactionPicker  ( onReact = { react ->
                viewModel.reactToMessage(channelID, msg.id, react)
               showPicker.value = false
           })
       }
   }

}

@Composable
fun ChatBubble(
    message: Message,
    onChatBubbleClick: () -> Unit,
    onReactionClicked: (String, String) -> Unit,
) {
    // Check if the message is sent by the current user
    val isCurrentUser = message.senderId == Firebase.auth.currentUser?.uid

    // State to show or hide the reaction picker


    // Bubble color based on the sender
    val bubbleColor = if (isCurrentUser) {
        bubbleColorOne
    } else {
        bubbleColorTwo
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable {
               onChatBubbleClick()
            }
    ) {
        val alignment = if (!isCurrentUser) Alignment.CenterStart else Alignment.CenterEnd
        Column(
            modifier = Modifier
                .padding(8.dp)
                .align(alignment),
            horizontalAlignment = if (!isCurrentUser) Alignment.Start else Alignment.End
        ) {
            // Show sender's name for messages not sent by the current user
            if (!isCurrentUser) {
                Text(
                    text = message.senderName,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }


            // Message bubble with text or image
            Box(
                modifier = Modifier
                    .background(color = bubbleColor, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    // Show image if available
                    if (message.imageUrl != null) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = message.message?.trim() ?: "", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    // Message timestamp
                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.createdAt),
                        color = Color.Black.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    if(message.reactions.isNotEmpty()){
                        ReactionRow(
                            reactions = message.reactions,
                            onReactionClicked = { react ->
                                onReactionClicked(message.id, react)
                            }
                        )
                    }
                }
            }

        }
    }
}


/*
 ReactionPicker creates a horizontal bar with rounded corners containing a set of reaction emojis.
 When an emoji is clicked, the onReact lambda function is called,
 passing the selected reaction to the parent composable.
 */
@Composable
fun ReactionPicker(onReact: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.5f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ReactionItem(reaction = "😂", imageRes = R.drawable.haha_emote, onReact)
        ReactionItem(reaction = "❤", imageRes = R.drawable.love_emote, onReact)
        ReactionItem(reaction = "😡", imageRes = R.drawable.angry_emote, onReact)
        ReactionItem(reaction = "😭", imageRes = R.drawable.sad_emote, onReact)
    }
}

/*
this ReactionItem function creates a clickable image that represents a specific reaction.
When the image is clicked, it invokes the onReact function, passing the reaction type as an argument. This allows you to handle user interactions with the reaction item,
such as updating the reaction count or performing other actions based on the selected reaction.
 */
@Composable
fun ReactionItem(reaction: String, imageRes: Int, onReact: (String) -> Unit) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = reaction,
        modifier = Modifier
            .size(32.dp)
            .clickable { onReact(reaction) }
    )
}

/*
ReactionRow function takes a map of reactions and users.
It then creates a horizontal row to display each reaction. For each reaction, it shows the emoji and the number of users who reacted with it.
The layout is styled with background color, padding, and rounded corners.
 */
@Composable
fun ReactionRow(reactions: Map<String, List<String>>, onReactionClicked: (String) -> Unit) {
    Row(
        modifier = Modifier
            .padding(top = 4.dp)
            .background(Color.LightGray.copy(alpha = 0.3f), shape = RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        reactions.forEach { (reaction, users) ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { onReactionClicked(reaction) }
            ) {
                Text(
                    text = "$reaction ${users.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}


@Composable
fun CallButton(isVideoCall: Boolean, onClick: (ZegoSendCallInvitationButton) -> Unit) {
    AndroidView(factory = { context ->
        val button = ZegoSendCallInvitationButton(context)
        button.setIsVideoCall(isVideoCall)
        button.resourceID = "zego_data"
        button
    }, modifier = Modifier.size(50.dp)) { zegoCallButton ->
        zegoCallButton.setOnClickListener { _ -> onClick(zegoCallButton) }
    }
}