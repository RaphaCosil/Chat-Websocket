package com.example.chatwebsocket

import com.google.gson.Gson

class Utils() {
    fun dismayedJson(json: String): MessageData {
        val gson = Gson()
        return gson.fromJson(json, MessageData::class.java)
    }

    fun makeJson(username: String, message: String): String {
        val gson = Gson()
        val json = MessageData(username, message)
        return gson.toJson(json)
    }
}