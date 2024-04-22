package com.example.chatwebsocket

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatwebsocket.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            sendForms()
        }
    }
    private fun sendForms(){
        val intent = Intent(this, MainActivity::class.java)
        if(binding.editTextIdRoom.text.isNotEmpty()){
            intent.putExtra("room", (binding.editTextIdRoom.text.toString()))
        }
        else{
            intent.putExtra("room", "1")
        }
        if(binding.editTextUserNameLogin.text.isNotEmpty()) {
            intent.putExtra("username", binding.editTextUserNameLogin.text.toString())
            startActivity(intent)
        }
        else{
            Toast.makeText(this, "Type an username", Toast.LENGTH_SHORT).show()
        }
    }
}