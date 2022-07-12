package com.etna.mycalendar.Models

import java.util.HashMap


class MyEventsModel(
    var eventId: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var startHour: String? = null,
    var endHour: String? = null,
    var eventDescription: String? = null,
    var sharedWith: HashMap<String,String>
){

    constructor(): this ("","","","","","", HashMap())
    constructor(mGroupId: String?, startDateString: String, endDateString: String, startHourString: String, endHourString: String, toString: String) : this()


}