package com.etna.mycalendar.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.etna.mycalendar.Activity.MainActivity
import com.etna.mycalendar.R
import com.etna.mycalendar.Models.UserModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_home_page.view.*

class HomePageFragment : Fragment() {
    private var fuser: FirebaseUser? = null
    private var handleCalendar: CardView? = null
    private var profilCardId: CardView? = null
    private  var messagerieCardViewId: CardView? = null
    private  var aboutCardViewId: CardView? = null
    private  var parrainageCardViewId: CardView? = null
    private var mBottomNav: BottomNavigationView? = null
    private val currentUserModel: UserModel? = null
    private var image_profile: CircleImageView? = null
    var username: TextView? = null
    var reference: DatabaseReference? = null
    private var storageReference: StorageReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)
        image_profile = view.findViewById(R.id.profile_image)
        username = view.findViewById(R.id.username)
        storageReference = FirebaseStorage.getInstance().getReference("uploads")
        fuser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid)
        handleCalendar = view.findViewById(R.id.handleCalendar)
        profilCardId = view.findViewById(R.id.profilCardId)
        messagerieCardViewId = view.findViewById(R.id.messagerieCardViewId)
        aboutCardViewId = view.findViewById(R.id.aboutCardViewId)
        parrainageCardViewId = view.findViewById(R.id.parrainageCardViewId)
        view.handleCalendar.setOnClickListener(View.OnClickListener {
            mBottomNav = (activity as MainActivity?)?.findViewById(R.id.bottomNavigation)
            val homeItem = mBottomNav!!.menu.getItem(1)
            mBottomNav!!.selectedItemId = homeItem.itemId
            val eventsFragment = DisplayEventsFragment()
            val fragmentManager = fragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.content, eventsFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        })
        view.profilCardId.setOnClickListener(View.OnClickListener {
            mBottomNav = (activity as MainActivity?)?.findViewById(R.id.bottomNavigation)
            val homeItem = mBottomNav!!.menu.getItem(4)
            mBottomNav!!.selectedItemId = homeItem.itemId
            val profilFragment = currentUserModel?.let { it1 -> ProfilFragment(it1) }
            val fragmentManager = fragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            profilFragment?.let { it1 -> fragmentTransaction.replace(R.id.content, it1) }
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        })
        view.messagerieCardViewId.setOnClickListener(View.OnClickListener {
            mBottomNav = (activity as MainActivity?)?.findViewById(R.id.bottomNavigation)
            val homeItem = mBottomNav!!.menu.getItem(3)
            mBottomNav!!.selectedItemId = homeItem.itemId
            val messagerieFragment = EventsMessagerieFragment()
            val fragmentManager = fragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.content, messagerieFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        })
        view.aboutCardViewId.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "Cette section est en cours de développement.",
                Toast.LENGTH_SHORT
            ).show()
        })
        view.parrainageCardViewId.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "Cette section est en cours de développement.",
                Toast.LENGTH_SHORT
            ).show()
        })
        return view
    }
}