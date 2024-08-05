package com.example.speechfinder.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.speachfinder.R
import com.example.speechfinder.MainActivity
import com.example.speechfinder.SharedPrefUtil
import com.example.speechfinder.data.LoginDetails
import com.example.speechfinder.sign_up.SignUpActivity

class LoginActivity : AppCompatActivity() {


    private lateinit var viewModel: AuthViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.login_button)
        val signup: TextView = findViewById(R.id.sign_up)



        signup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            val loginDetails = LoginDetails(email, password)

            viewModel.loginUser(loginDetails).observe(this) { result ->
                result.fold(
                    onSuccess = { data ->

                        SharedPrefUtil.saveToken(applicationContext, data.token)
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                        if(data.status==0){
                            showEmailConfirmationDialog(data.token)
                        }
                        else{
                            startActivity(Intent(this,MainActivity::class.java))

                        }
                    },
                    onFailure = {
                        Toast.makeText(this, "Login Failed: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            }
        }
    }


    private fun showEmailConfirmationDialog(token: String) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_email_confirmation, null)
        val codeEditText = dialogLayout.findViewById<EditText>(R.id.confirmation_code_edit_text)

        builder.setView(dialogLayout)
            .setPositiveButton("Confirm") { dialog, _ ->
                val code = codeEditText.text.toString()
                viewModel.verifyEmail(code, token).observe(this, Observer { result ->
                    result.fold(
                        onSuccess = {
                            Toast.makeText(this, "Email Verified Successfully", Toast.LENGTH_SHORT)
                                .show()
                            dialog.dismiss()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        },
                        onFailure = {
                            Toast.makeText(
                                this,
                                "Email Verification Failed: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                })
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}