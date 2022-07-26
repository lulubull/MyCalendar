package com.etna.mycalendar.Adapter

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.Models.MyEventsModel
import com.etna.mycalendar.R
import com.google.firebase.database.*
import java.lang.StringBuilder


class MyEventsAdapter(mContext: Context?, mEvents: MutableList<MyEventsModel?>?) :
    RecyclerView.Adapter<MyEventsAdapter.ViewHolder>() {
    private var mContext: Context? = mContext
    private var mEvents: MutableList<MyEventsModel?>? = mEvents
    private val mUsernames: List<String>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.events_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val eventsModel: MyEventsModel? = mEvents?.get(position)
        val builder: StringBuilder = StringBuilder()

        Log.d("contentBuilder1", "" + builder.toString())
        holder.dateDebut.setText(eventsModel?.startDate)
        holder.dateFin.setText(eventsModel?.endDate)
        holder.startHour.setText(eventsModel?.startHour)
        holder.endHour.setText(eventsModel?.endHour)
        holder.description.setText(eventsModel?.eventDescription)
        holder.dateDebutTextView.visibility = View.VISIBLE
        holder.dateDebutTextView.text = "Date de début :"
        holder.dateFinTextView.visibility = View.VISIBLE
        holder.dateFinTextView.text = "Date de fin :"
        holder.startHourTextView.visibility = View.VISIBLE
        holder.startHourTextView.text = "Heure de début :"
        holder.endHourTextView.visibility = View.VISIBLE
        holder.endHourTextView.text = "Heure de fin :"
        holder.descriptionTextView.visibility = View.VISIBLE
        holder.descriptionTextView.text = "Description de l'événement :"

        //shared event
        builder.setLength(0)
        Log.d("contentBuilder3", "" + builder.toString())
        holder.sharedWithLinearLayout.visibility = View.VISIBLE
        holder.iconIsShared.visibility = View.VISIBLE
        holder.principalLinearLayout.setBackgroundColor(Color.parseColor("#9B59B6"))
        holder.secondaryLinearLayout.setBackgroundColor(Color.parseColor("#9B59B6"))
        holder.principalRelativeLayout.setBackgroundColor(Color.parseColor("#9B59B6"))
        for (value in eventsModel?.sharedWith!!) {
            Log.d("contentValue", "" + value)
            _getUsername(holder, value.toString(), builder)
        }
        holder.sharedWithTextView.text = "Partagé avec :"
        holder.sharedWithTextView.visibility = View.VISIBLE
        holder.sharedWith.visibility = View.VISIBLE

    }

    override fun getItemCount(): Int {
        return mEvents!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dateDebut: TextView
        var dateFin: TextView
        var startHour: TextView
        var endHour: TextView
        var description: TextView
        var iconIsShared: ImageView
        var dateDebutTextView: TextView
        var dateFinTextView: TextView
        var startHourTextView: TextView
        var endHourTextView: TextView
        var descriptionTextView: TextView
        var sharedWith: TextView
        var sharedWithTextView: TextView
        var principalLinearLayout: LinearLayout
        var secondaryLinearLayout: LinearLayout
        var sharedWithLinearLayout: LinearLayout
        var principalRelativeLayout: RelativeLayout

        init {
            dateDebut = itemView.findViewById(R.id.dateDebut)
            dateFin = itemView.findViewById(R.id.dateFin)
            startHour = itemView.findViewById(R.id.startHour)
            endHour = itemView.findViewById(R.id.endHour)
            description = itemView.findViewById(R.id.description)
            principalLinearLayout = itemView.findViewById(R.id.principalLinearLayout)
            secondaryLinearLayout = itemView.findViewById(R.id.secondaryLinearLayout)
            principalRelativeLayout = itemView.findViewById(R.id.principalRelativeLayout)
            sharedWithLinearLayout = itemView.findViewById(R.id.sharedWithLinearLayout)
            sharedWithTextView = itemView.findViewById(R.id.sharedWithTextView)
            sharedWith = itemView.findViewById(R.id.sharedWith)
            dateDebutTextView = itemView.findViewById(R.id.dateDebutTextView)
            dateFinTextView = itemView.findViewById(R.id.dateFinTextView)
            startHourTextView = itemView.findViewById(R.id.startHourTextView)
            endHourTextView = itemView.findViewById(R.id.endHourTextView)
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView)
            iconIsShared = itemView.findViewById(R.id.event_item_logo_shared)
        }
    }

    private fun _getUsername(
        holder: ViewHolder,
        valueReceived: String,
        builderReceived: StringBuilder
    ) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("inDataChanged", "inDataChanged")
                for (snapshot in dataSnapshot.children) {
                    val userInfo: UserModel? = snapshot.getValue(UserModel::class.java)
                    if (userInfo?.id.equals(valueReceived)) {
                        builderReceived.append(userInfo?.username)
                        builderReceived.append(" ")
                        holder.sharedWith.text = builderReceived.toString()
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}