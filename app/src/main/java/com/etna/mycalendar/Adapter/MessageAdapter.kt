package com.etna.mycalendar.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.etna.mycalendar.Models.ChatModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MessageAdapter(mContext: Context, mChatModel: MutableList<ChatModel?>?, imageurl: String) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder?>() {
    private val mContext: Context = mContext
    private val mChatModel: MutableList<ChatModel?>? = mChatModel
    private val imageurl: String = imageurl
    private var fuser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == MSG_TYPE_RIGHT) {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false)
            ViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatModel: ChatModel? = mChatModel?.get(position)
        holder.showMessage.setText(chatModel?.message)
        if (imageurl == "default") {
            holder.profileImage.setImageResource(R.mipmap.ic_launcher)
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profileImage)
        }
        if (position == mChatModel?.size!! - 1) {
            if (chatModel!!.isIsseen) {
                holder.txtSeen.setText("Vu")
            } else {
                holder.txtSeen.setText("Envoyé")
            }
        } else {
            holder.txtSeen.setVisibility(View.GONE)
        }
    }

    override fun getItemCount(): Int {
        return mChatModel!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var showMessage: TextView
        var profileImage: ImageView
        var txtSeen: TextView

        init {
            showMessage = itemView.findViewById<TextView>(R.id.show_message)
            profileImage = itemView.findViewById(R.id.profile_image)
            txtSeen = itemView.findViewById<TextView>(R.id.txt_seen)
        }
    }

    override fun getItemViewType(position: Int): Int {
        fuser = FirebaseAuth.getInstance().getCurrentUser()
        return if (mChatModel?.get(position)?.sender.equals(fuser?.getUid())) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    companion object {
        const val MSG_TYPE_LEFT = 0
        const val MSG_TYPE_RIGHT = 1
    }
}