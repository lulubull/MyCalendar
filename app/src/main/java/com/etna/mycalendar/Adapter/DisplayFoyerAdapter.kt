package com.etna.mycalendar.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.etna.mycalendar.Activity.AddEventActivity
import com.etna.mycalendar.Activity.DisplayUserFoyerAgendaActivity
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView

class DisplayFoyerAdapter(mContext: Context, mUserModels: MutableList<UserModel?>?) :
    RecyclerView.Adapter<DisplayFoyerAdapter.ViewHolder>() {
    private val mContext: Context = mContext
    private val mUserModels: MutableList<UserModel?>? = mUserModels

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.user_of_foyer_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userModel: UserModel? = mUserModels?.get(position)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val referenceTo = FirebaseDatabase.getInstance().getReference("Users").child(
            firebaseUser!!.uid
        ).child("requests").child("to").child(userModel?.id.toString())
        val referenceFrom =
            FirebaseDatabase.getInstance().getReference("Users").child(userModel?.id.toString())
                .child("requests").child("from").child(
                    firebaseUser.uid
                )
        val popup = PopupMenu(mContext, holder.imageButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.display_foyer_profil_menu, popup.menu)
        holder.firstAndLastname.setText(userModel?.prenom.toString() + " " + userModel?.nom)
        holder.whichCountry.setText(
            userModel?.ville.toString() + " " + userModel?.codePostal
        )
        if (userModel?.imageURL.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher)
        } else {
            Glide.with(mContext).load(userModel?.imageURL).into(holder.profile_image)
        }
        popup.menu.getItem(0).title = "Consulter l'agenda"
        popup.menu.getItem(0).isEnabled = true
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.seeAgenda -> {
                    val intentFoyerAgenda = Intent(
                        mContext,
                        DisplayUserFoyerAgendaActivity::class.java
                    )
                    intentFoyerAgenda.putExtra("idUser", userModel?.id)
                    mContext.startActivity(intentFoyerAgenda)
                    return@OnMenuItemClickListener true
                }
                R.id.addEvent -> {
                    val addEvent = Intent(mContext, AddEventActivity::class.java)
                    addEvent.putExtra("idUser", userModel?.id)
                    mContext.startActivity(addEvent)
                    return@OnMenuItemClickListener true
                }
            }
            true
        })
        holder.imageButton.setOnClickListener { popup.show() }
    }

    override fun getItemCount(): Int {
        return mUserModels?.size!!
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var firstAndLastname: TextView
        var whichCountry: TextView
        var profile_image: CircleImageView
        var imageButton: ImageButton

        init {
            firstAndLastname = itemView.findViewById(R.id.firstAndLastname)
            profile_image = itemView.findViewById(R.id.profile_image)
            imageButton = itemView.findViewById(R.id.imageButton)
            whichCountry = itemView.findViewById(R.id.whichCountry)
        }
    }
}
