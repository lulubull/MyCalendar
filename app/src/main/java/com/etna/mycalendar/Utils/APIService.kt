package com.etna.mycalendar.Utils

import retrofit2.http.POST
import com.etna.mycalendar.Notifications.Sender
import com.etna.mycalendar.Notifications.MyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers

interface APIService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:AIzaSyCDqCjoN3wVYD08ZB_AdE3qYAfU94WMCrI"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<MyResponse?>?
}