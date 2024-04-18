package com.example.chatwebsocket

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatwebsocket.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var webSocketListener: WebSocketListener
    private lateinit var mainViewModel: MainViewModel
    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val utils = Utils()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Geting username and room id from LoginActivity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val username= intent.extras?.getString("username")
        val roomId= intent.extras?.getString("room")
        binding.chattitle.text= ">You're in: ${roomId}"



        //Connecting to data base to get saved messages
        val database = Firebase.database
        val reference = database.getReference("messages")


        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val databaseValue = childSnapshot.getValue(String::class.java)
                        val messageDataDatabase= databaseValue?.let {utils.dismayedJson(it) }
                        var oldText=""
                        if(messageDataDatabase?.username==username) {
                            oldText =
                                binding.textMessages.text.toString() + ">You: ${messageDataDatabase?.message}\n"
                        }
                        else{
                            binding.textMessages.text.toString() + ">${messageDataDatabase?.username}: ${messageDataDatabase?.message}\n"

                        }
                        binding.textMessages.text= oldText
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) = Unit
        })

        if (username != null) {
            if (roomId != null) {
                connectWebSocket(roomId, username)
            }
        }

        //Observing changes in the message LiveData
        mainViewModel.message.observe(this) {
            var text = binding.textMessages.text.toString()
            text +=
                if (it.first) {
                    ">you: ${it.second.message}\n"
                } else {
                    if (it.second.username == username) {
                        ">you: ${it.second.message}\n"

                    } else {
                        ">${it.second.username}: ${it.second.message}\n"
                    }
                }
            binding.textMessages.text = text
        }

        //Sending message to server
        binding.buttonSend.setOnClickListener {

            val insertToDatabase =reference.push()
            if(binding.sendMessageText.text.toString().isNotEmpty()){
                val messageText= binding.sendMessageText.text.toString()
                val jsonMessage= utils.makeJson(username.toString(), messageText)
                webSocket?.send(jsonMessage)

                insertToDatabase.setValue(jsonMessage)
//                mainViewModel.setMessage(Pair(true, binding.sendMessageText.text.toString()))
                binding.sendMessageText.text.clear()
            }
            else{
                Toast.makeText(this, "Type something", Toast.LENGTH_SHORT).show()
            }
        }

    }
    //Disconnect user from server when he closes this activity
    override fun onStop() {
        super.onStop()
        val username= intent.extras?.getString("username")
        webSocket?.send("$username left the chat")
        webSocket?.close(1000, "$username left the chat")
    }
    //Disconnect the user from the server when he clicks the back button on his phone
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val username= intent.extras?.getString("username")
        webSocket?.send("$username left the chat")
        webSocket?.close(1000, "$username left the chat")
    }

    //Creates the request to the server
    private fun createRequest(webSocketUrl:String): Request {
        return Request.Builder()
            .url(webSocketUrl)
            .build()
    }
    @SuppressLint("SetTextI18n")
    private fun connectWebSocket(roomId:String, username: String){
        //Creating tje connection with the server
        val webSocketUrl = "wss://free.blr2.piesocket.com/v3/${roomId}?api_key=mnz7ic1KF5deNSqSneGJdq5UhEzMYdqma4qYCc2m&notify_self=1"

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        webSocketListener = WebSocketListener(mainViewModel)

        mainViewModel.socketStatus.observe(this) {
            val oldtext = binding.textMessages.text.toString()
            binding.textMessages.text =
                oldtext + "\n" + if (it) "You was connected\n" else "You was disconnected\n"
        }

        webSocket = okHttpClient.newWebSocket(createRequest(webSocketUrl)
            ,webSocketListener )
        val jsonMessage= utils.makeJson(username, "joined the chat")
        webSocket?.send(jsonMessage)
    }
}