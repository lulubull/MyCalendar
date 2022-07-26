package com.etna.mycalendar.Adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.etna.mycalendar.Activity.MessageActivity
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class EventsRequestSendedAdapter(mContext: Context?, mUserModels: List<UserModel>?) :
    RecyclerView.Adapter<EventsRequestSendedAdapter.ViewHolder?>() {
    private val mContext: Context? = mContext
    private val mUserModels: List<UserModel>? = mUserModels

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext)
            .inflate(R.layout.events_request_sended_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userModel: UserModel = mUserModels!![position]
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().getCurrentUser()!!
        val referenceTo: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid())
                .child("requests").child("to").child(userModel.id.toString())
        val referenceFrom: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(userModel.id.toString())
                .child("requests").child("from").child(firebaseUser.getUid())
        holder.username.setText(userModel.username)
        if (userModel.imageURL.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher)
        } else {
            Glide.with(mContext!!).load(userModel.imageURL).into(holder.profile_image)
        }
        referenceTo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    val requestType: String? = dataSnapshot.child("requestType").getValue<String>(
                        String::class.java
                    )
                    if (requestType == "accepted") {
                        holder.message.setVisibility(View.GONE)
                        holder.requestAcceptedTextView.setVisibility(View.VISIBLE)
                        holder.cancelButton.text = "Envoyer un message"
                        holder.cancelButton.setBackgroundResource(R.drawable.bg_button_generic_ui)
                        holder.cancelButton.setOnClickListener {
                            val intent = Intent(mContext, MessageActivity::class.java)
                            intent.putExtra("userid", userModel.id)
                            mContext?.startActivity(intent)
                        }
                        holder.deleteButton.visibility = View.VISIBLE
                        holder.deleteButton.setBackgroundResource(R.drawable.bg_button_cancel_ui)
                        holder.deleteButton.setOnClickListener {
                            AlertDialog.Builder(mContext!!)
                                .setTitle("Etes vous sur ?")
                                .setMessage(
                                    "Souhaitez vous supprimer " + userModel.username
                                        .toString() + " de votre liste ?"
                                )
                                .setPositiveButton(
                                    "Annuler la demande",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(
                                            dialogInterface: DialogInterface,
                                            i: Int
                                        ) {
                                            referenceTo.removeValue()
                                            referenceFrom.removeValue()
                                        }
                                    })
                                .setNegativeButton(
                                    "Retour",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface, which: Int) {
                                            dialog.dismiss()
                                        }
                                    })
                                .show()
                        }
                    } else if (requestType == "send") {
                        holder.requestAcceptedTextView.setVisibility(View.GONE)
                        holder.message.setVisibility(View.VISIBLE)
                        holder.cancelButton.setBackgroundResource(R.drawable.bg_button_cancel_ui)
                        holder.deleteButton.visibility = View.GONE
                        holder.cancelButton.visibility = View.VISIBLE
                        holder.cancelButton.setOnClickListener {
                            AlertDialog.Builder(mContext!!)
                                .setTitle("Etes vous sur ?")
                                .setMessage(
                                    "Vous avez envoyé une demande à " + userModel.username
                                        .toString() + ". Souhaitez vous annuler votre demande ?"
                                )
                                .setPositiveButton(
                                    "Annuler la demande",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(
                                            dialogInterface: DialogInterface,
                                            i: Int
                                        ) {
                                            referenceTo.removeValue()
                                            referenceFrom.removeValue()
                                        }
                                    })
                                .setNegativeButton(
                                    "Retour",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface, which: Int) {
                                            dialog.dismiss()
                                        }
                                    })
                                .show()
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var requestAcceptedTextView: TextView
        var message: TextView
        var profile_image: ImageView
        var cancelButton: Button
        var deleteButton: Button

        init {
            username = itemView.findViewById<TextView>(R.id.username)
            requestAcceptedTextView = itemView.findViewById<TextView>(R.id.requestAcceptedTextView)
            message = itemView.findViewById<TextView>(R.id.message)
            profile_image = itemView.findViewById(R.id.profile_image)
            cancelButton = itemView.findViewById(R.id.cancelButton)
            deleteButton = itemView.findViewById(R.id.deleteButton)
        }
    }

    override fun getItemCount(): Int {
        return 0
    }
}