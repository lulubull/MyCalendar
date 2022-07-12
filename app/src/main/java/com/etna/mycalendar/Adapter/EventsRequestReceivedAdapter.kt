package com.etna.mycalendar.Adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.etna.mycalendar.R
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.Activity.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.HashMap

class EventsRequestReceivedAdapter(mContext: Context?, mUserModels: List<UserModel>?) :
    RecyclerView.Adapter<EventsRequestReceivedAdapter.ViewHolder?>() {
    /** Déclaration de variables  */
    private val mContext: Context? = mContext
    private val mUserModels: List<UserModel>? = mUserModels

    /**
     * Création du view holder. On récupère les item à injecter
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext)
            .inflate(R.layout.events_request_received_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * Fonction permettant de contrôler chaque élément de nos vues xml
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userModel: UserModel = mUserModels!![position]
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().getCurrentUser()
        val referenceTo: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(userModel.id.toString())
                .child("requests").child("to").child(firebaseUser?.getUid().toString())
        val referenceFrom: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser?.getUid().toString())
                .child("requests").child("from").child(userModel?.id.toString())
        holder.username.setText(userModel.username)
        if (userModel.imageURL.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher)
        } else {
            Glide.with(mContext!!).load(userModel.imageURL).into(holder.profile_image)
        }
        holder.cancelButton.setOnClickListener { }
        referenceFrom.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    val requestType: String? = dataSnapshot.child("requestType").getValue<String>(
                        String::class.java
                    )
                    Log.d("dsValue", dataSnapshot.getValue().toString())
                    if (requestType == "received") {
                        holder.acceptButton.setBackgroundResource(R.drawable.bg_button_accepted_ui)
                        holder.acceptButton.setOnClickListener { // Si il accepte il faut passer le status dans from de received à accepted
                            // du coup
                            val hashMapFrom = HashMap<String, String>()
                            hashMapFrom["requestType"] = "accepted"
                            referenceFrom.setValue(hashMapFrom)
                            referenceTo.setValue(hashMapFrom)
                        }
                        holder.cancelButton.setBackgroundResource(R.drawable.bg_button_cancel_ui)
                        holder.cancelButton.text = "Refuser"
                        holder.cancelButton.setOnClickListener {
                            val hashMapFrom = HashMap<String, String>()
                            hashMapFrom["requestType"] = "refused"
                            referenceFrom.setValue(hashMapFrom)
                        }
                    } else if (requestType == "accepted") {
                        holder.acceptButton.setBackgroundResource(R.drawable.bg_button_generic_ui)
                        holder.acceptButton.setOnClickListener {
                            val intent = Intent(mContext, MessageActivity::class.java)
                            intent.putExtra("userid", userModel.id)
                            mContext?.startActivity(intent)
                        }
                        holder.acceptButton.text = "Envoyer un message"
                        holder.acceptButton.isEnabled = true
                        holder.cancelButton.setBackgroundResource(R.drawable.bg_button_cancel_ui)
                        holder.cancelButton.text = "Supprimer"
                        holder.cancelButton.setOnClickListener {
                            AlertDialog.Builder(mContext!!)
                                .setTitle("Etes vous sur ?")
                                .setMessage(
                                    "Etes vous sur de vouloir supprimer " + userModel.id
                                        .toString() + " de votre liste ?"
                                )
                                .setPositiveButton(
                                    "Supprimer",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(
                                            dialogInterface: DialogInterface,
                                            i: Int
                                        ) {
                                            referenceFrom.removeValue()
                                        }
                                    })
                                .setNegativeButton(
                                    "Annuler",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface, which: Int) {}
                                    })
                                .show()
                        }
                    } else if (requestType == "refused") {
                        holder.acceptButton.setBackgroundResource(R.drawable.bg_button_accepted_ui)
                        holder.acceptButton.setOnClickListener {
                            AlertDialog.Builder(mContext!!)
                                .setTitle("Etes vous sur ?")
                                .setMessage("\"Vous pouvez encore accepter la demande qui vous a été envoyé. Si vous souhaitez supprimer définitivement cette demande, appuyez sur \"Supprimer\". Si vous souhaitez accepter cette demande, appuyez sur \"Accepter\"")
                                .setPositiveButton(
                                    "Accepter",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(
                                            dialogInterface: DialogInterface,
                                            i: Int
                                        ) {
                                            val hashMapFrom = HashMap<String, String>()
                                            hashMapFrom["requestType"] = "accepted"
                                            referenceFrom.setValue(hashMapFrom)
                                        }
                                    })
                                .setNegativeButton(
                                    "Supprimer",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(dialog: DialogInterface, which: Int) {
                                            referenceFrom.removeValue()
                                        }
                                    })
                                .show()
                        }
                        holder.acceptButton.text = "Accepter"
                        holder.acceptButton.setBackgroundResource(R.drawable.bg_button_accepted_ui)
                        holder.cancelButton.text = "Déjà refusée"
                        holder.cancelButton.setBackgroundResource(R.drawable.bg_button_cancel_ui)
                        holder.cancelButton.isEnabled = false
                    }
                } else if (dataSnapshot.getValue() == null) {
                    holder.acceptButton.setOnClickListener { }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    /**
     * Retourne le nombre d'item présent dans mUserModels
     * @return
     */


    /**
     * Fonction permettant de récuperer les éléments XML de l'item 'events_request_received_item.xml'
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var profile_image: ImageView
        var cancelButton: Button
        var acceptButton: Button

        init {
            username = itemView.findViewById<TextView>(R.id.username)
            profile_image = itemView.findViewById(R.id.profile_image)
            acceptButton = itemView.findViewById(R.id.acceptButton)
            cancelButton = itemView.findViewById(R.id.cancelButton)
        }
    }

    override fun getItemCount(): Int {
        return mUserModels?.size!!
    }

    /** Constructeur  */

}