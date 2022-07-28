package com.etna.mycalendar.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.etna.mycalendar.R
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.etna.mycalendar.Models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.HashMap

class UserProfilViewActivity : AppCompatActivity() {
    private var reference: DatabaseReference? = null
    private var requestType: String? = null
    private var idUser: String? = null
    private var imageProfile: ImageView? = null
    private var username: TextView? = null
    private var pseudo: TextView? = null
    private var closeButton: Button? = null
    private var sendRequestButton: Button? = null
    private var disponibilityButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profil_view)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        idUser = intent.getSerializableExtra("idUser") as String?
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val referenceTo = FirebaseDatabase.getInstance().getReference("Users").child(
            firebaseUser?.uid.toString()
        ).child("requests").child("to").child(idUser!!)
        val referenceFrom = FirebaseDatabase.getInstance().getReference("Users").child(
            idUser!!
        ).child("requests").child("from").child(firebaseUser?.uid.toString())
        imageProfile = findViewById(R.id.imageViewUser)
        username = findViewById(R.id.usernameTextView)
        pseudo = findViewById(R.id.pseudoTextView)
        closeButton = findViewById(R.id.closeButton)
        sendRequestButton = findViewById(R.id.sendRequest)
        disponibilityButton = findViewById(R.id.disponibility)
        reference = FirebaseDatabase.getInstance().getReference("Users").child(idUser!!)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userModel: UserModel? = dataSnapshot.getValue(UserModel::class.java)
                username?.setText(userModel?.prenom.toString() + " " + userModel?.nom)
                pseudo?.setText("(" + userModel?.username.toString() + ")")
                if (userModel?.imageURL.equals("default")) {
                    imageProfile?.setImageResource(R.mipmap.ic_launcher)
                } else {
                    Glide.with(applicationContext).load(userModel?.imageURL).into(imageProfile!!)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        referenceTo.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    requestType = dataSnapshot.child("requestType").getValue(String::class.java)
                    if (requestType != null) {
                        if (requestType == "send") {
                            sendRequestButton?.setText("Envoyée")
                            sendRequestButton?.setEnabled(false)
                        } else if (requestType == "accepted") {
                            sendRequestButton?.setOnClickListener(View.OnClickListener {
                                val intent = Intent(
                                    applicationContext,
                                    MessageActivity::class.java
                                )
                                intent.putExtra("userid", idUser)
                                startActivity(intent)
                            })
                        }
                    }
                } else {
                    sendRequestButton?.setOnClickListener(View.OnClickListener {
                        val hashMapTo = HashMap<String, String>()
                        hashMapTo["requestType"] = "send"
                        val hashMapFrom = HashMap<String, String>()
                        hashMapFrom["requestType"] = "received"
                        referenceTo.setValue(hashMapTo)
                        referenceFrom.setValue(hashMapFrom)
                    })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        disponibilityButton?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this@UserProfilViewActivity,
                "Acces refusé à l'agenda",
                Toast.LENGTH_SHORT
            ).show()
        })
        closeButton?.setOnClickListener(View.OnClickListener { onBackPressed() })
    }
}
