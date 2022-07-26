package com.etna.mycalendar.Activity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.etna.mycalendar.R
import com.etna.mycalendar.Adapter.MessageAdapter
import com.etna.mycalendar.Models.ChatModel
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.Notifications.Client
import com.etna.mycalendar.Notifications.Data
import com.etna.mycalendar.Notifications.MyResponse
import com.etna.mycalendar.Notifications.Sender
import com.etna.mycalendar.Notifications.Token
import com.etna.mycalendar.Utils.APIService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList
import java.util.HashMap

class MessageActivity : AppCompatActivity() {
    var profile_image: CircleImageView? = null
    var username: TextView? = null
    var fuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    var btn_send: ImageButton? = null
    var text_send: EditText? = null
    var messageAdapter: MessageAdapter? = null
    var mChatModel: MutableList<ChatModel?>? = null
    var recyclerView: RecyclerView? = null
    var seenListener: ValueEventListener? = null
    var userid: String? = null
    var apiService: APIService? = null
    var notify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        getSupportActionBar()?.setTitle("")
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        apiService = Client.getClient("https://fcm.googleapis.com/")?.create(APIService::class.java)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(getApplicationContext())
        linearLayoutManager.stackFromEnd = true
        recyclerView!!.layoutManager = linearLayoutManager
        profile_image = findViewById<CircleImageView>(R.id.profile_image)
        username = findViewById<TextView>(R.id.username)
        btn_send = findViewById<ImageButton>(R.id.btn_send)
        text_send = findViewById<EditText>(R.id.text_send)
        intent = getIntent()
        userid = intent.getStringExtra("userid")
        fuser = FirebaseAuth.getInstance().currentUser
        btn_send!!.setOnClickListener {
            notify = true
            val msg = text_send!!.text.toString()
            if (msg != "") {
                sendMessage(fuser!!.uid, userid.toString(), msg)
            } else {
                Toast.makeText(
                    this@MessageActivity,
                    "Le message est vide",
                    Toast.LENGTH_SHORT
                ).show()
            }
            text_send!!.setText("")
        }
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid!!)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userModel: UserModel? = dataSnapshot.getValue(UserModel::class.java)
                username?.setText(userModel?.username)
                if (userModel?.imageURL.equals("default")) {
                    profile_image!!.setImageResource(R.mipmap.ic_launcher)
                } else {
                    Glide.with(getApplicationContext()).load(userModel!!.imageURL)
                        .into(profile_image!!)
                }
                readMessages(fuser!!.uid, userid!!, userModel!!.imageURL)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        seenMessage(userid!!)
    }

    open fun seenMessage(userid: String) {
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        seenListener = reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val chatModel: ChatModel? = snapshot.getValue(ChatModel::class.java)
                    if (chatModel?.receiver.equals(fuser!!.uid) && chatModel?.sender
                            .equals(userid)
                    ) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        snapshot.ref.updateChildren(hashMap)
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    open fun sendMessage(sender: String, receiver: String, message: String) {
        var reference = FirebaseDatabase.getInstance().reference
        val hashMap = HashMap<String, Any>()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["message"] = message
        hashMap["isseen"] = false
        reference.child("Chats").push().setValue(hashMap)
        val chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(fuser!!.uid)
            .child(userid!!)
        val chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(userid!!)
            .child(fuser!!.uid)
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        chatRef2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef2.child("id").setValue(fuser!!.uid)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userModel: UserModel? = dataSnapshot.getValue(UserModel::class.java)
                if (notify) {
                    sendNotification(receiver, userModel!!.username, message)
                }
                notify = false
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    open fun sendNotification(receiver: String, username: String, message: String) {
        val tokens = FirebaseDatabase.getInstance().getReference("Tokens")
        val query = tokens.orderByKey().equalTo(receiver)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val token: Token? = snapshot.getValue(Token::class.java)
                    val data = Data(
                        fuser!!.uid, R.mipmap.ic_launcher, "$username: $message", "Nouveau Message",
                        userid
                    )
                    val sender = Sender(data, token?.token.toString())
                    apiService?.sendNotification(sender)
                        ?.enqueue(object : Callback<MyResponse> {
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if (response.code() == 200) {
                                    if (response.body()?.success === 1) {
                                        Log.d("SuccessOnSendMess", "Success")
                                        Toast.makeText(
                                            this@MessageActivity,
                                            "Envoyé  !",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else if (response.body()?.success !== 1) {
                                        // Toast.makeText(MessageActivity.this, "Erreur lors de l'envoie  !", Toast.LENGTH_SHORT).show();
                                        Log.d("ErrorOnSendMess", "Error")
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                                TODO("Not yet implemented")
                            }
                        })
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    open fun readMessages(myid: String, userid: String, imageurl: String) {
        mChatModel = ArrayList<ChatModel?>()
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mChatModel!!.clear()
                for (snapshot in dataSnapshot.children) {
                    val chatModel: ChatModel? = snapshot.getValue(ChatModel::class.java)
                    if (chatModel?.receiver.equals(myid) && chatModel?.sender
                            .equals(userid) ||
                        chatModel?.receiver.equals(userid) && chatModel?.sender.equals(myid)
                    ) {
                        mChatModel!!.add(chatModel)
                    }
                    messageAdapter = MessageAdapter(this@MessageActivity, mChatModel, imageurl)
                    recyclerView!!.adapter = messageAdapter
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    open fun status(status: String) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reference!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        status("online")
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListener!!)
        status("offline")
    }
}

private fun Any?.enqueue(callback: Callback<MyResponse>) {

}

