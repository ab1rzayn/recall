package com.example.recalls.Auth.SignUp

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.recall.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject



@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<SignUpState>(SignUpState.Nothing)
    val state = _state.asStateFlow()


    private val _userDetails = MutableStateFlow<List<User>>(emptyList())
    val userDetails  =_userDetails.asStateFlow()
    private val db =  FirebaseFirestore.getInstance()

    fun signUp(name: String, email: String, password: String, phone: String) {
        _state.value = SignUpState.Loading
        // Firebase signIn
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.user?.let { firebaseUser->
                        firebaseUser.updateProfile(
                            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                        ).addOnCompleteListener {

                            if(it.isSuccessful){
                                saveUserDetails(firebaseUser.uid, name, email, phone)
                                _state.value = SignUpState.Success
                            }else{
                                _state.value = SignUpState.Error
                            }
                        }
                    }

                } else {
                    _state.value = SignUpState.Error
                }
            }
    }

    fun saveUserDetails(userId: String, name: String, email: String, phone: String){
        val user = User( id = userId, name =name, email=email, phone=phone)


        db.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User Details has been Added")
            }
            .addOnFailureListener{ e->
                Log.w("Firestore", "Error adding document", e)
            }
    }


}

sealed class SignUpState {
    object Nothing : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    object Error : SignUpState()
}