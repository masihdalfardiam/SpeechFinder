package com.example.speechfinder.sign_up

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.speechfinder.data.User
import com.example.speechfinder.repository.UserRepository

class SignupViewModel : ViewModel() {
    private val userRepository = UserRepository()

    fun registerUser(user: User): LiveData<Result<Boolean>> {
        return userRepository.registerUser(user)
    }
}