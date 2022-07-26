package com.etna.mycalendar.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.etna.mycalendar.R

class SendEmailActivity : AppCompatActivity() {
    private var eTo: EditText? = null
    private var eSubject: EditText? = null
    private var eMsg: EditText? = null
    private var btn: Button? = null
    private val emailUser: String? = null
    private var btnClose: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_email)
        eTo = findViewById(R.id.txtTo)
        eSubject = findViewById(R.id.txtSub)
        eMsg = findViewById(R.id.txtMsg)
        btn = findViewById(R.id.btnSend)
        btnClose = findViewById(R.id.closeButton)
        btnClose?.setOnClickListener(View.OnClickListener { onBackPressed() })
        btn?.setOnClickListener(View.OnClickListener {
            val it = Intent(Intent.ACTION_SEND)
            it.putExtra(Intent.EXTRA_EMAIL, arrayOf(eTo?.getText().toString()))
            it.putExtra(Intent.EXTRA_SUBJECT, eSubject?.getText().toString())
            it.putExtra(Intent.EXTRA_TEXT, eMsg?.getText())
            it.type = "message/rfc822"
            startActivity(Intent.createChooser(it, "Choisissez votre client mail"))
        })
    }
}
