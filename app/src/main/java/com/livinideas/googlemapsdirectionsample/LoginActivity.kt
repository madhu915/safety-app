package com.livinideas.googlemapsdirectionsample

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        onLogin()
    }

    fun onLogin() {
        loginBtn.setOnClickListener() {
            if(loginEt.text.toString() == "" || passwordEt.text.toString() == "")
                Toast.makeText(this,"Field(s) should not be empty!",Toast.LENGTH_SHORT).show()
            else {
                val myIntent = Intent(this, MainActivity::class.java)
                myIntent.putExtra("key1", "login")
                this.startActivity(myIntent)
            }
        }
    }
}