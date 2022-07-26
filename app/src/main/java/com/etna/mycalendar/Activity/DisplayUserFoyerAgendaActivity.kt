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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.etna.mycalendar.Adapter.MyEventsAdapter
import com.etna.mycalendar.Models.MyEventsModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DisplayUserFoyerAgendaActivity: AppCompatActivity() {
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
        _readEvents()
    }

    private fun _readEvents() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
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
                _placeEventsOnList()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun _placeEventsOnList() {
        val activity: Activity = this@DisplayUserFoyerAgendaActivity
        if (activity != null) {
            if (mEvents?.size!! > 0) {
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
