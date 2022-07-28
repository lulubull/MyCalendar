package com.etna.mycalendar.Activity

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.etna.mycalendar.Fragments.HomePageFragment
import com.etna.mycalendar.Fragments.NotificationsFragment
import com.etna.mycalendar.Fragments.ProfilUtilisateurFragment
import com.etna.mycalendar.Fragments.DisplayEventsFragment
import com.etna.mycalendar.Fragments.EventsMessagerieFragment
import com.etna.mycalendar.R
import com.etna.mycalendar.Models.UserModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var firebaseUser: FirebaseUser? = null
    private var reference: DatabaseReference? = null
    private var currentUserModel: UserModel? = null
    private val fragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser?.uid.toString())
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currentUserModel = dataSnapshot.getValue(UserModel::class.java)
                username.setText(currentUserModel?.username)

            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            var fragment: Fragment? = null
            when (item.itemId) {
                R.id.home ->
                    fragment = HomePageFragment()
                R.id.calendriers ->
                    fragment = DisplayEventsFragment()
                R.id.notifications ->
                    fragment = NotificationsFragment()
                R.id.contacts ->
                    fragment = EventsMessagerieFragment()
                R.id.profil ->
                    fragment = currentUserModel?.let { ProfilUtilisateurFragment(it) }
            }
            val transaction = this@MainActivity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.content, fragment!!).commit()
            true
        })
        val startFragment: Fragment = HomePageFragment()
        loadFragment(startFragment)
    }

    private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.GENERIC_LOG_OUT_FR))
                    .setMessage(getString(R.string.GENERIC_YOU_WANT_TO_LOG_OUT_ARE_YOU_SURE_FR))
                    .setPositiveButton(
                        getString(R.string.GENERIC_YES_FR)
                    ) { dialogInterface, i ->
                        FirebaseAuth.getInstance().signOut()
                        startActivity(
                            Intent(this@MainActivity, LoginActivity::class.java).setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                            )
                        )
                    }
                    .setNegativeButton(
                        getString(R.string.GENERIC_NO_FR)
                    ) { dialog, which -> dialog.dismiss() }
                    .show()
                return true
            }
        }
        return false
    }
}