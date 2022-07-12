package com.etna.mycalendar.Models

class ChatModel {
    var sender: String? = null
    var receiver: String? = null
    var message: String? = null
    var isIsseen = false
        private set

    constructor(sender: String?, receiver: String?, message: String?, isseen: Boolean) {
        this.sender = sender
        this.receiver = receiver
        this.message = message
        isIsseen = isseen
    }

    constructor() {}

    fun setIsseen(isseen: Boolean) {
        isIsseen = isseen
    }
}