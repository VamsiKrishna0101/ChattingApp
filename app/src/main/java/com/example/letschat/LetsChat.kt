package com.example.letschat

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class LetsChat : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lets_chat)
        verifyUserLoggedIn()
        val signout=findViewById<Button>(R.id.signout_btn)
        signout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent=Intent(this,MainActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        val newmsg=findViewById<Button>(R.id.msgnow)
        newmsg.setOnClickListener {
            val intent=Intent(this,NewMessagesActivity::class.java)
            startActivity(intent)
        }
    }
    private fun verifyUserLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

}
