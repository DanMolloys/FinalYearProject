package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalyearprojectdm.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    //declare binding to xml
    private lateinit var binding: ActivityRegisterBinding

    //declare fireBase link
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //intilize binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

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
                                // Create a User object
                                val user = User(firebaseAuth.currentUser!!.uid, email)

                                // Add the user to the Firestore database
                                firestore.collection("users").document(user.id).set(user)
                                    .addOnSuccessListener {
                                        //send to signInPage
                                        val intent = Intent(this, SingInActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error adding user to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "Error creating user: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
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