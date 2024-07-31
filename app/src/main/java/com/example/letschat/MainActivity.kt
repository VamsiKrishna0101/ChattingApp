package com.example.letschat

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val register = findViewById<Button>(R.id.continue_signup)
        register.setOnClickListener {
            registernow()
        }

        val alreadyAccount = findViewById<TextView>(R.id.already_account)
        alreadyAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val imageButton = findViewById<Button>(R.id.imagebutton)
        imageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            val imageButton = findViewById<Button>(R.id.imagebutton)
            imageButton.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun registernow() {
        val email = findViewById<EditText>(R.id.email_signup)
        val myEmail = email.text.toString()
        val password = findViewById<EditText>(R.id.password_signup)
        val myPassword = password.text.toString()
        val username = findViewById<EditText>(R.id.username_signup)
        val myUsername = username.text.toString()

        if (myEmail.isEmpty() || myPassword.isEmpty() || myUsername.isEmpty()) {
            Toast.makeText(this, "Please Enter All Necessary Fields", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(myEmail, myPassword)
            .addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                Toast.makeText(this, "Created Account. Please Login", Toast.LENGTH_SHORT).show()
                uploadImageToFirebase(myUsername, myEmail, myPassword)
                Log.d("MainActivity", "createUserWithEmail:success")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create account: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebase(username: String, email: String, password: String) {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("Images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveUserToFirebaseDatabase(uri.toString(), username, email, password)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String, username: String, email: String, password: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(email, username, profileImageUrl, password)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "User details submitted")
                val intent=Intent(this,LetsChat::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("MainActivity", "Failed to submit user details: ${it.message}")
            }
    }
}

data class User(val email: String, val username: String, val profileImageUrl: String, val password: String)
