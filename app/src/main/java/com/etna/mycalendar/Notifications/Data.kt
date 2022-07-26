package com.etna.mycalendar.Notifications

class Data {
    private var user: String? = null
    private var icon = 0
    private var body: String? = null
    var title: String? = null
    private var sented: String? = null

    constructor(user: String?, icon: Int, body: String?, title: String?, sented: String?) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.sented = sented
    }

    constructor() {}
}