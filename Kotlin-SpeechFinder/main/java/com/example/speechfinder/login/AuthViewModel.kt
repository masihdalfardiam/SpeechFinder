package com.example.speechfinder.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.speechfinder.data.Data
import com.example.speechfinder.data.LoginDetails
import com.example.speechfinder.repository.UserRepository

class AuthViewModel: ViewModel() {

    private val userRepository = UserRepository()


    fun loginUser(loginDetails: LoginDetails): LiveData<Result<Data>> {
        return userRepository.loginUser(loginDetails)
    }

    fun verifyEmail(code: String, token: String): LiveData<Result<Boolean>> {
        return userRepository.verifyEmail(code, token)
    }
}