package com.example.chatwebsocket

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.chatwebsocket.databinding.ActivityMainBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var webSocketListener: WebSocketListener
    private lateinit var mainViewModel: MainViewModel
    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val username= intent.extras?.getString("username")
        val roomId= intent.extras?.getString("room")
        binding.chattitle.text= ">You're in: ${roomId}"

        val webSocketUrl = "wss://free.blr2.piesocket.com/v3/${roomId}?api_key=mnz7ic1KF5deNSqSneGJdq5UhEzMYdqma4qYCc2m&notify_self=1"

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        webSocketListener = WebSocketListener(mainViewModel)

        mainViewModel.socketStatus.observe(this , Observer {
            binding.textMessages.text = if (it) "User connected" else "User disconnected"
        })

        webSocket = okHttpClient.newWebSocket(createRequest(webSocketUrl)
            ,webSocketListener )
        val jsonMessage= makeJson(username.toString(), "joined the chat")
        webSocket?.send(jsonMessage)


        var text = ""
        mainViewModel.message.observe(this, Observer {
            text += if(it.first) {
//                val message:String = it.second
//                val dataMessage = dismayedJson(message)
                ">${it.second.username}: ${it.second.message}\n"
            } else{
//                val message = it.second
//                val dataMessage = dismayedJson(message)
                ">${it.second.username}: ${it.second.message}\n"
            }
            binding.textMessages.text=text
        })

//        binding.buttonConnect.setOnClickListener {
//            webSocket = okHttpClient.newWebSocket(createRequest(webSocketUrl)
//                ,webSocketListener )
//            webSocket?.send("$username joined the chat")
//
//        }
//        binding.buttonDisconnect.setOnClickListener {
//            webSocket?.close(1000, "Cancelled Manualy")
//        }
        binding.buttonSend.setOnClickListener {
            if(binding.sendMessageText.text.toString().isNotEmpty()){
                val messageText= binding.sendMessageText.text.toString()
//                webSocket?.send(username + ": "+binding.sendMessageText.text.toString())
                val jsonMessage= makeJson(username.toString(), messageText)
                webSocket?.send(jsonMessage)

//                mainViewModel.setMessage(Pair(true, binding.sendMessageText.text.toString()))
                binding.sendMessageText.text.clear()
            }
            else{
                Toast.makeText(this, "Type something", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onStop() {
        super.onStop()
        val username= intent.extras?.getString("username")
        webSocket?.send("$username left the chat")
        webSocket?.close(1000, "$username left the chat")
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val username= intent.extras?.getString("username")
        webSocket?.send("$username left the chat")
        webSocket?.close(1000, "$username left the chat")
    }
    private fun createRequest(webSocketUrl:String): Request {
        return Request.Builder()
            .url(webSocketUrl)
            .build()
    }
    private fun makeJson(username: String, text: String): String {
        val gson = Gson()
        val json = MessageData(username, text)
        return gson.toJson(json)
    }
    private fun dismayedJson(json: String): MessageData {
        val gson = Gson()
        return gson.fromJson(json, MessageData::class.java)
    }
}