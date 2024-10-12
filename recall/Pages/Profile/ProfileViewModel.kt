package com.example.recall.Pages.Profile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.recall.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    //state to hold the user profile data
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile = _userProfile.asStateFlow()

    init {
        //Fetch the User Data when the viewModel is created
        fetchUserProfile()

    }

    private fun fetchUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name") ?: ""
                    val createdAt = documentSnapshot.getLong("createdAt") ?: System.currentTimeMillis()
                    val email = documentSnapshot.getString("email") ?: ""
                    val phone = documentSnapshot.getString("phone") ?: ""
                    _userProfile.value = User(name = name, CreatedAt = createdAt, email = email, phone = phone)
                } else {
                    Log.w("ProfileViewModel", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.w("ProfileViewModel", "Error fetching user profile", e)
            }
    }

    fun updateUserProfile(name: String?, email: String?, phone: String?){
        val userId = FirebaseAuth.getInstance().currentUser?.uid?:return

        val updates = mutableMapOf<String, Any>()
        name?.let { updates["name"] = it }
        email?.let { updates["email"] = it }
        phone?.let { updates["phone"] = it }


        if(updates.isNotEmpty()){
            db.collection("users").document(userId).update(updates)
                .addOnSuccessListener {
                    fetchUserProfile()
            }.addOnFailureListener{e->
                Log.w("Error updating user profile", e)
            }
        }
    }

    fun logout(){
        FirebaseAuth.getInstance().signOut()
    }

}