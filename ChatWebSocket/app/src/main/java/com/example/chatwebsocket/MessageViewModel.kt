package com.example.chatwebsocket

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MessageViewModel: ViewModel() {
    private val _socketStatus = MutableLiveData(false)
    val socketStatus: LiveData<Boolean> = _socketStatus

    private val _message = MutableLiveData<Pair<Boolean, MessageData>>()
    val message: LiveData<Pair<Boolean,MessageData>> =_message

    fun setStatus(status: Boolean) = GlobalScope.launch(Dispatchers.Main){
        _socketStatus.value = status
    }
    fun setMessage(message:Pair<Boolean, MessageData>) = GlobalScope.launch(Dispatchers.Main){
        if(_socketStatus.value == true){
            _message.value = message
        }
    }
}