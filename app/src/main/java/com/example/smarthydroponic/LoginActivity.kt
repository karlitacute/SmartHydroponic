package com.example.smarthydroponic

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {
    lateinit var googleSignInClient: GoogleSignInClient
    val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogle = findViewById<LinearLayout>(R.id.btnGoogle)

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)

            val email = account.email

            Toast.makeText(this, "Login berhasil: $email", Toast.LENGTH_SHORT).show()


        } catch (e: ApiException) {
            Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
        }
    }
}