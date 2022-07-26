package com.etna.mycalendar.Activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.etna.mycalendar.R
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.etna.mycalendar.Adapter.MyEventsAdapter
import com.etna.mycalendar.Models.MyEventsModel
import com.etna.mycalendar.Activity.AddEventActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList

class DisplayUserFoyerAgendaActivity
/** Constructeur de la classe DisplayUserFoyerAgendaActivity  */
    : AppCompatActivity() {
    /** Déclaration variables  */
    private var addEventButton: Button? = null
    private var scrollView: NestedScrollView? = null
    private var gridEvents: RecyclerView? = null
    private var myEventsAdapter: MyEventsAdapter? = null
    private var mEvents: MutableList<MyEventsModel?>? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mProgressView: View? = null
    private var startRelativeLayout: LinearLayout? = null
    private var noResultsLayout: SwipeRefreshLayout? = null
    private var idUserBundle: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_user_foyer_agenda)
        idUserBundle = intent.getSerializableExtra("idUser") as String?

        /*overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);*/
        /** Initialisation des variables  */
        addEventButton = findViewById(R.id.addEventButton)
        scrollView = findViewById(R.id.scrollView)
        noResultsLayout = findViewById(R.id.noResultsLayout)
        gridEvents = findViewById(R.id.listView)
        gridEvents?.setHasFixedSize(true)
        gridEvents?.setLayoutManager(LinearLayoutManager(applicationContext))
        gridEvents?.setNestedScrollingEnabled(false)
        startRelativeLayout = findViewById(R.id.startRelativeLayout)
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh_items)
        mProgressView = findViewById(R.id.progressBar)
        mEvents = ArrayList()
        addEventButton?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@DisplayUserFoyerAgendaActivity, AddEventActivity::class.java)
            intent.putExtra("idUser", idUserBundle)
            startActivity(intent)
        })
        noResultsLayout?.setOnRefreshListener(OnRefreshListener { _readEvents() })
        mSwipeRefreshLayout?.setOnRefreshListener(OnRefreshListener { _readEvents() })

        /* if (bundleArg == null) {
            _readUsers();
        } else {
            ArrayList<UserModel> myNewList = new ArrayList<>();
            myNewList.addAll(bundleArg.getParcelableArrayList("listUsers"));
            applyFilter(myNewList);
        }*/_readEvents()
    }

    /**
     * Fonction permettant de récuperer tout les Evenements de l'utilisateur
     */
    private fun _readEvents() {
        //_showProgress(true)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        /** Reference sur la table Events  */
        val reference = FirebaseDatabase.getInstance().getReference("Events").child(
            idUserBundle!!
        )
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mEvents!!.clear()
                for (snapshot in dataSnapshot.children) {
                    val eventModel = snapshot.getValue(MyEventsModel::class.java)
                    assert(firebaseUser != null)
                    mEvents!!.add(eventModel)
                }
                myEventsAdapter = MyEventsAdapter(applicationContext, mEvents)
                gridEvents!!.adapter = myEventsAdapter
                /** On liste tout les evenements  */
                _placeEventsOnList()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    /**
     * Fonction permettant d'afficher les evenements
     */
    private fun _placeEventsOnList() {
        val activity: Activity = this@DisplayUserFoyerAgendaActivity
        if (activity != null) {
            if (mEvents?.size!! > 0) {
                //_showProgress(false)
                mSwipeRefreshLayout!!.isRefreshing = false
                noResultsLayout!!.isRefreshing = false
                noResultsLayout!!.visibility = View.GONE
            } else {
                mProgressView!!.visibility = View.GONE
                mSwipeRefreshLayout!!.visibility = View.GONE
                noResultsLayout!!.visibility = View.VISIBLE
                noResultsLayout!!.isRefreshing = false
            }
        }
    }

    /**
     * Fait apparaître le Spinner/Loader et cache différentes interfaces
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun _showProgress(show: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
            mSwipeRefreshLayout!!.visibility = if (show) View.GONE else View.VISIBLE
            mSwipeRefreshLayout!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                ((if (show) 1 else 0.toFloat()) as Int).toFloat()
            ).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mSwipeRefreshLayout!!.visibility = if (show) View.GONE else View.VISIBLE
                }
            })
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                ((if (show) 1 else 0.toFloat()) as Int).toFloat()
            ).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mSwipeRefreshLayout!!.visibility = if (show) View.GONE else View.VISIBLE
        }
    }
}
