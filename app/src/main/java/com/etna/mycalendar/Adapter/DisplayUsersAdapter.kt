package com.etna.mycalendar.Adapter
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.etna.mycalendar.R
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.etna.mycalendar.Activity.MessageActivity
import com.etna.mycalendar.Activity.UserProfilViewActivity
import com.etna.mycalendar.Models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import java.util.HashMap

class DisplayUsersAdapter(mContext: Context?, mUserModels: MutableList<UserModel?>?) :
    RecyclerView.Adapter<DisplayUsersAdapter.ViewHolder>() {
    var mContext: Context? = mContext
    private var mUserModels: MutableList<UserModel?>? = mUserModels

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.user_to_find_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userModel: UserModel? = mUserModels?.get(position)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        // Demande d'ami envoyé par utilisateur courant ==> utilisateur selectionné
        val referenceTo = FirebaseDatabase.getInstance().getReference("Users").child(
            firebaseUser!!.uid
        ).child("requests").child("to").child(userModel?.id.toString())
        // Demande d'ami reçu par utilisateur selectionné <== utilisateur courant
        val referenceFrom =
            FirebaseDatabase.getInstance().getReference("Users").child(userModel?.id.toString())
                .child("requests").child("from").child(
                    firebaseUser.uid
                )
        // Demande d'ami envoyé par l'utilisateur selectionné ==> utilisateur courant
        val referenceToUser =
            FirebaseDatabase.getInstance().getReference("Users").child(userModel?.id.toString())
                .child("requests").child("to").child(
                    firebaseUser.uid
                )
        val popup = PopupMenu(mContext!!, holder.imageButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.display_user_profil_menu, popup.menu)
        holder.firstAndLastname.setText(userModel?.prenom.toString() + " " + userModel?.nom)
        holder.whichCountry.setText(
            userModel?.ville.toString() + " " + userModel?.codePostal
        )
        if (userModel?.imageURL.equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher)
        } else {
            Glide.with(mContext!!).load(userModel?.imageURL).into(holder.profile_image)
        }
        // Firebase : Demande d'ami envoyé par utilisateur courant ==> utilisateur selectionné
        referenceTo.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    val requestType = dataSnapshot.child("requestType").getValue(
                        String::class.java
                    )
                    if (requestType == "send") {
                        popup.menu.getItem(1).title = "Demande envoyée !"
                        popup.menu.getItem(1).isEnabled = false
                        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.seeProfil -> {
                                    val intentProfil = Intent(
                                        mContext,
                                        UserProfilViewActivity::class.java
                                    )
                                    intentProfil.putExtra("idUser", userModel?.id)
                                    mContext!!.startActivity(intentProfil)
                                    return@OnMenuItemClickListener true
                                }
                            }
                            true
                        })
                    } else if (requestType == "accepted") {
                        popup.menu.getItem(1).title = "Demande acceptée"
                        popup.menu.getItem(1).isEnabled = true
                        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.seeProfil -> {
                                    val intentProfil = Intent(
                                        mContext,
                                        UserProfilViewActivity::class.java
                                    )
                                    intentProfil.putExtra("idUser", userModel?.id)
                                    mContext!!.startActivity(intentProfil)
                                    return@OnMenuItemClickListener true
                                }
                                R.id.sendRequest -> {
                                    val intentMessage = Intent(
                                        mContext,
                                        MessageActivity::class.java
                                    )
                                    intentMessage.putExtra("userid", userModel?.id)
                                    mContext!!.startActivity(intentMessage)
                                    return@OnMenuItemClickListener true
                                }
                            }
                            true
                        })
                    } else if (requestType == "refused") {
                        popup.menu.getItem(1).title = "Votre demande a été refusée"
                        popup.menu.getItem(1).isEnabled = false
                        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.seeProfil -> {
                                    val intentProfil = Intent(
                                        mContext,
                                        UserProfilViewActivity::class.java
                                    )
                                    intentProfil.putExtra("idUser", userModel?.id)
                                    mContext!!.startActivity(intentProfil)
                                    return@OnMenuItemClickListener true
                                }
                            }
                            true
                        })
                    }
                } else if (dataSnapshot.value == null) {
                    popup.menu.getItem(1).title = "Envoyer une demande"
                    popup.menu.getItem(1).isEnabled = true
                    popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.seeProfil -> {
                                val intentProfil = Intent(
                                    mContext,
                                    UserProfilViewActivity::class.java
                                )
                                intentProfil.putExtra("idUser", userModel?.id)
                                mContext!!.startActivity(intentProfil)
                                return@OnMenuItemClickListener true
                            }
                            R.id.sendRequest -> {
                                val hashMapTo = HashMap<String, String>()
                                hashMapTo["requestType"] = "send"
                                val hashMapFrom = HashMap<String, String>()
                                hashMapFrom["requestType"] = "received"
                                referenceTo.setValue(hashMapTo)
                                referenceFrom.setValue(hashMapFrom)
                                return@OnMenuItemClickListener true
                            }
                        }
                        true
                    })
                }
                // Firebase : Demande d'ami envoyé par l'utilisateur selectionné ==> utilisateur courant
                referenceToUser.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value != null) {
                            val requestType = dataSnapshot.child("requestType").getValue(
                                String::class.java
                            )
                            if (requestType == "received") {
                                popup.menu.getItem(1).title = "Demande envoyée !"
                                popup.menu.getItem(1).isEnabled = false
                                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                                    when (item.itemId) {
                                        R.id.seeProfil -> {
                                            val intentProfil = Intent(
                                                mContext,
                                                UserProfilViewActivity::class.java
                                            )
                                            intentProfil.putExtra("idUser", userModel?.id)
                                            mContext!!.startActivity(intentProfil)
                                            return@OnMenuItemClickListener true
                                        }
                                    }
                                    true
                                })
                            } else if (requestType == "accepted") {
                                popup.menu.getItem(1).title = "Demande acceptée"
                                popup.menu.getItem(1).isEnabled = true
                                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                                    when (item.itemId) {
                                        R.id.seeProfil -> {
                                            val intentProfil = Intent(
                                                mContext,
                                                UserProfilViewActivity::class.java
                                            )
                                            intentProfil.putExtra("idUser", userModel?.id)
                                            mContext!!.startActivity(intentProfil)
                                            return@OnMenuItemClickListener true
                                        }
                                        R.id.sendRequest -> {
                                            val intentMessage = Intent(
                                                mContext,
                                                MessageActivity::class.java
                                            )
                                            intentMessage.putExtra("userid", userModel?.id)
                                            mContext!!.startActivity(intentMessage)
                                            return@OnMenuItemClickListener true
                                        }
                                    }
                                    true
                                })
                            } else if (requestType == "refused") {
                                popup.menu.getItem(1).title = "Demande refusée"
                                popup.menu.getItem(1).isEnabled = false
                                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                                    when (item.itemId) {
                                        R.id.seeProfil -> {
                                            val intentProfil = Intent(
                                                mContext,
                                                UserProfilViewActivity::class.java
                                            )
                                            intentProfil.putExtra("idUser", userModel?.id)
                                            mContext!!.startActivity(intentProfil)
                                            return@OnMenuItemClickListener true
                                        }
                                    }
                                    true
                                })
                            }
                        } else if (dataSnapshot.value == null) {
                            popup.menu.getItem(1).title = "Envoyer une demande"
                            popup.menu.getItem(1).isEnabled = true
                            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                                when (item.itemId) {
                                    R.id.seeProfil -> {
                                        val intentProfil = Intent(
                                            mContext,
                                            UserProfilViewActivity::class.java
                                        )
                                        intentProfil.putExtra("idUser", userModel?.id)
                                        mContext!!.startActivity(intentProfil)
                                        return@OnMenuItemClickListener true
                                    }
                                    R.id.sendRequest -> {
                                        val hashMapTo = HashMap<String, String>()
                                        hashMapTo["requestType"] = "send"
                                        val hashMapFrom = HashMap<String, String>()
                                        hashMapFrom["requestType"] = "received"
                                        referenceTo.setValue(hashMapTo)
                                        referenceFrom.setValue(hashMapFrom)
                                        return@OnMenuItemClickListener true
                                    }
                                }
                                true
                            })
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        holder.imageButton.setOnClickListener { popup.show() }
    }

    /**
     * Retourne le nombre d'item présent dans mUserModels
     * @return
     */
    override fun getItemCount(): Int {
        return mUserModels?.size!!
    }

    /**
     * Fonction permettant de récuperer les éléments XML de l'item 'user_to_find_item.xml'
     */
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

    /** Constructeur  */

}
