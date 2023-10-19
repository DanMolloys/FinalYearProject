package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalyearprojectdm.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    //declare binding to xml
    private lateinit var binding: ActivityRegisterBinding

    //declare fireBase link
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //intilize binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmedPassword = binding.etConfirmPassword.text.toString()


            //validation
            if (email.isNotEmpty() && password.isNotEmpty() && confirmedPassword.isNotEmpty()) {
                if (password.equals(confirmedPassword)) {

                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                //send to signInPage
                                val intent = Intent(this, SingInActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()

                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords are not a match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Inputs must not be blank", Toast.LENGTH_SHORT).show()

            }
        }

        //if already button
        binding.tvAlreadyReg.setOnClickListener {
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
        }
    }
}