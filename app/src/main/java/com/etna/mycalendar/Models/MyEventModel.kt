package com.etna.mycalendar.Models

import java.util.HashMap


data class MyEventsModel(
    var eventId:String?="",
    var startDate: String? = "",
    var endDate: String? = "",
    var startHour: String? = "",
    var endHour: String? = "",
    var eventDescription: String? = "",
    var sharedWith: HashMap<String, String>?
){

    constructor(): this ("","","","","","", HashMap())




}