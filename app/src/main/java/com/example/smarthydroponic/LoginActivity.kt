package com.example.smarthydroponic

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {
    lateinit var googleSignInClient: GoogleSignInClient
    val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<LinearLayout>(R.id.btnGoogle)

        val btnEye = findViewById<ImageView>(R.id.btnEye)

        var isPasswordVisible = true

        btnEye.setOnClickListener {

            if (isPasswordVisible) {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                btnEye.setImageResource(R.drawable.ic_mata)
            } else {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                btnEye.setImageResource(R.drawable.ic_matatutup)
            }

            etPassword.setSelection(etPassword.text.length)

            isPasswordVisible = !isPasswordVisible
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua field", Toast.LENGTH_SHORT).show()
            } else {
                val name = email.substringBefore("@")

                saveUser(name, email, "")

                Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                goToHome()
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogle.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
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

            val name = account.displayName ?: account.email?.substringBefore("@") ?: ""
            val email = account.email ?: ""
            val photo = account.photoUrl?.toString() ?: ""

            saveUser(name, email, photo)

            Toast.makeText(this, "Login berhasil: $email", Toast.LENGTH_SHORT).show()
            goToHome()

        } catch (e: ApiException) {
            Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveUser(name: String, email: String, photo: String) {
        val sp = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val editor = sp.edit()

        editor.putString("NAME", name)
        editor.putString("EMAIL", email)
        editor.putString("PHOTO", photo)
        editor.apply()
    }
    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}