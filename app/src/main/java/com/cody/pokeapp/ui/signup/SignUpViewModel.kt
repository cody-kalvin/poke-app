package com.cody.pokeapp.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.cody.pokeapp.api.RequestState
import com.cody.pokeapp.util.SharedPrefUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val sharedPref: SharedPrefUtil
) : ViewModel() {
    val email: MutableLiveData<String> = MutableLiveData()

    val password: MutableLiveData<String> = MutableLiveData()

    private val _signUpState: MutableLiveData<RequestState<FirebaseUser>> =
        MutableLiveData(RequestState.Initial)
    val signUpState: LiveData<RequestState<FirebaseUser>> = _signUpState

    fun signUp() {
        _signUpState.value = RequestState.Loading

        val email = email.value ?: ""
        val password = password.value ?: ""
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                val user = auth.currentUser
                when {
                    task.isSuccessful && user != null -> {
                        retrieveToken(user)
                    }
                    task.isSuccessful && user == null -> {
                        _signUpState.value = RequestState.Failure("User not logged in")
                    }
                    else -> {
                        _signUpState.value =
                            RequestState.Failure(task.exception?.message ?: "Unknown error")
                    }
                }
            }
    }

    private fun retrieveToken(user: FirebaseUser) {
        user.getIdToken(true)
            .addOnCompleteListener { task ->
                val token = task.result?.token
                if (task.isSuccessful && !token.isNullOrBlank()) {
                    sharedPref.tokenId = token
                    _signUpState.value = RequestState.Success(user)
                } else {
                    _signUpState.value =
                        RequestState.Failure(task.exception?.message ?: "Unknown error")
                }
            }
    }
}