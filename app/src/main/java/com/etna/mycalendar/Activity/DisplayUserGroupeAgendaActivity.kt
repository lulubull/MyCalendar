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

class DisplayUserGroupeAgendaActivity: AppCompatActivity() {
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
        setContentView(R.layout.activity_display_user_groupe_agenda)
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
            val intent = Intent(this@DisplayUserGroupeAgendaActivity, AddEventActivity::class.java)
            intent.putExtra("idUser", idUserBundle)
            startActivity(intent)
        })
        noResultsLayout?.setOnRefreshListener(OnRefreshListener { readEvents() })
        mSwipeRefreshLayout?.setOnRefreshListener(OnRefreshListener { readEvents() })
        readEvents()
    }

    private fun readEvents() {
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
                placeEventsOnList()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun placeEventsOnList() {
        val activity: Activity = this@DisplayUserGroupeAgendaActivity
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
}
