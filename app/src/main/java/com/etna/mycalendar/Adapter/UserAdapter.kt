package com.etna.mycalendar.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.etna.mycalendar.Activity.MessageActivity
import com.etna.mycalendar.Models.ChatModel
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class UserAdapter(mContext: Context, mUserModels: MutableList<UserModel?>?, ischat: Boolean) :
    RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext: Context = mContext
    private val mUserModels: MutableList<UserModel?>? = mUserModels
    private val ischat: Boolean = ischat
    private var theLastMessage: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userModel: UserModel? = mUserModels?.get(position)
        holder.username.setText(userModel?.username)
        if (userModel?.imageURL.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher)
        } else {
            Glide.with(mContext).load(userModel?.imageURL).into(holder.profile_image)
        }
        if (ischat) {
            lastMessage(userModel?.id.toString(), holder.last_msg)
        } else {
            holder.last_msg.setVisibility(View.GONE)
        }
        if (ischat) {
            if (userModel?.status.equals("online")) {
                holder.img_on.visibility = View.VISIBLE
                holder.img_off.visibility = View.GONE
            } else {
                holder.img_on.visibility = View.GONE
                holder.img_off.visibility = View.VISIBLE
            }
        } else {
            holder.img_on.visibility = View.GONE
            holder.img_off.visibility = View.GONE
        }
        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(mContext, MessageActivity::class.java)
            intent.putExtra("userid", userModel?.id)
            mContext.startActivity(intent)
        })
    }

    override fun getItemCount(): Int {
        return mUserModels?.size!!
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var profile_image: ImageView
        val img_on: ImageView
        val img_off: ImageView
        val last_msg: TextView

        init {
            username = itemView.findViewById<TextView>(R.id.username)
            profile_image = itemView.findViewById(R.id.profile_image)
            img_on = itemView.findViewById(R.id.img_on)
            img_off = itemView.findViewById(R.id.img_off)
            last_msg = itemView.findViewById<TextView>(R.id.last_msg)
        }
    }

    private fun lastMessage(userid: String, last_msg: TextView) {
        theLastMessage = "default"
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().getCurrentUser()
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.getChildren()) {
                    val chatModel: ChatModel? = snapshot.getValue(ChatModel::class.java)
                    if (chatModel?.receiver
                            .equals(firebaseUser?.getUid()) && chatModel?.sender
                            .equals(userid) ||
                        chatModel?.receiver.equals(userid) && chatModel?.sender
                            .equals(firebaseUser?.getUid())
                    ) {
                        theLastMessage = chatModel?.message
                    }
                }
                when (theLastMessage) {
                    "default" -> last_msg.setText("Pas de message")
                    else -> last_msg.setText(theLastMessage)
                }
                theLastMessage = "default"
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}