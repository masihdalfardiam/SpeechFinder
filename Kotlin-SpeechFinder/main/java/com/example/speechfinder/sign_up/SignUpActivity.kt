package com.example.speechfinder.sign_up

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.speachfinder.R
import com.example.speechfinder.data.User
import com.example.speechfinder.login.LoginActivity

class SignUpActivity : AppCompatActivity() {


    private lateinit var viewModel: SignupViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]

        val emailEditText: EditText = findViewById(R.id.email)
        val firstNameEditText: EditText = findViewById(R.id.first_name)
        val lastNameEditText: EditText = findViewById(R.id.last_name)
        val passwordEditText: EditText = findViewById(R.id.password)
        val signupButton: Button = findViewById(R.id.signup_button)
        val login: TextView = findViewById(R.id.login)



        login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        signupButton.setOnClickListener {

            val email = emailEditText.text.toString()
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val user = User(email, firstName, lastName, password)

            viewModel.registerUser(user).observe(this) { result ->
                result.fold(
                    onSuccess = {
                        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        Toast.makeText(
                            this,
                            "Registration Failed: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}