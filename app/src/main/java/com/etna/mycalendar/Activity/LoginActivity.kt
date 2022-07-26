package com.etna.mycalendar.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        loginbtn.setOnClickListener{
            if(mail.text.trim().toString().isNotEmpty() || password.text.trim().toString().isNotEmpty()) {
                signInUser(mail.text.trim().toString(),password.text.trim().toString())
            }
        }
        btRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun signInUser(email:String, password:String){

        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    startActivity(Intent(this, MainActivity::class.java))
                }
                else {
                    Toast.makeText(this, "email ou mot de Passe Incorrect", Toast.LENGTH_SHORT).show()
                }
            }
    }
}