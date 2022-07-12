package com.etna.mycalendar.Activity

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.Menu
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
    /** Déclaration de variables  */

    private var firebaseUser: FirebaseUser? = null
    private var reference: DatabaseReference? = null
    private var currenUserModel: UserModel? = null
    private val fragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /** Fonction utilisée pour apporter une transition entre les activitées  */
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        /** On set la toolbar  */
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("")

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser?.uid.toString())
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                currenUserModel = dataSnapshot.getValue(UserModel::class.java)
                username.setText(currenUserModel?.username)

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            var fragment: Fragment? = null
            when (item.itemId) {
                R.id.home ->
                    /** HomePageFragment | Même fragment utilisée pour la page d'accueil */
                    /** HomePageFragment | Même fragment utilisée pour la page d'accueil */
                    fragment = HomePageFragment()
                R.id.calendriers ->
                    /** DisplayEventsFragment | Utilisée pour afficher les évènements et calendrier de l'utilisateur */
                    /** DisplayEventsFragment | Utilisée pour afficher les évènements et calendrier de l'utilisateur */
                    fragment = DisplayEventsFragment()
                R.id.notifications ->
                    /** Même et unique Fragment utilisée pour recevoir des notifications en tout genre */
                    /** Même et unique Fragment utilisée pour recevoir des notifications en tout genre */
                    fragment = NotificationsFragment()
                R.id.contacts ->
                    /** Messagerie / Contacts */
                    /** Messagerie / Contacts */
                    fragment = EventsMessagerieFragment()
                R.id.profil ->
                    /** Même et unique Fragment pour la gestion du profil de l'utilisateur  */
                    /** Même et unique Fragment pour la gestion du profil de l'utilisateur  */
                    fragment = currenUserModel?.let { ProfilUtilisateurFragment(it) }
            }
            val transaction = this@MainActivity.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.content, fragment!!).commit()
            true
        })
        /** Start Activity with HomePageFragment  */
        val startFragment: Fragment = HomePageFragment()
        _loadFragment(startFragment)
    }

    /**
     * 1 paramètre
     * Fonction permettant de charger le Fragment en fonction du choix effectué par l'utilisateur sur la bar du menu
     * @param fragment
     * @return
     */
    private fun _loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commit()
            return true
        }
        return false
    }

    /**
     * A tester | Afin de savoir si encore utilisée ou pas
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    /**
     * 1 paramètre
     * Fonction permettant de rediriger l'utilisateur sur l'inferface souhaitait | Permet également la déconnexion
     * @param item
     * @return
     */
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



