package com.example.personaltasks.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personaltasks.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signInBt.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass  = binding.passEt.text.toString().trim()
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { goToMain() }
                .addOnFailureListener { Toast.makeText(this, "Erro: ${it.message}", Toast.LENGTH_SHORT).show() }
        }

        binding.registerBt.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass  = binding.passEt.text.toString().trim()
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { goToMain() }
                .addOnFailureListener { Toast.makeText(this, "Erro: ${it.message}", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}