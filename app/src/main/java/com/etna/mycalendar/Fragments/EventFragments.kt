package com.etna.mycalendar.Fragments

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.etna.mycalendar.Activity.AddEventActivity
import com.etna.mycalendar.Adapter.MyEventsAdapter
import com.etna.mycalendar.Models.MyEventsModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList

class EventsFragment
    : Fragment() {
    private var filterButton: Button? = null
    private var addEventButton: Button? = null
    private var scrollView: NestedScrollView? = null
    private var gridEvents: RecyclerView? = null
    private var myEventsAdapter: MyEventsAdapter? = null
    private var mEvents: MutableList<MyEventsModel?>? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mProgressView: View? = null
    private var startRelativeLayout: LinearLayout? = null
    private var noResultsLayout: SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        filterButton = view.findViewById(R.id.filterButton)
        addEventButton = view.findViewById(R.id.addEventButton)
        scrollView = view.findViewById(R.id.scrollView)
        noResultsLayout = view.findViewById(R.id.noResultsLayout)
        gridEvents = view.findViewById(R.id.listView)
        gridEvents?.setHasFixedSize(true)
        gridEvents?.setLayoutManager(LinearLayoutManager(context))
        gridEvents?.setNestedScrollingEnabled(false)
        startRelativeLayout = view.findViewById(R.id.startRelativeLayout)
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items)
        mProgressView = view.findViewById(R.id.progressBar)
        mEvents = ArrayList()
        filterButton?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "Pas encore dev",
                Toast.LENGTH_SHORT
            ).show()
        })
        addEventButton?.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, AddEventActivity::class.java)
            startActivity(intent)
        })
        noResultsLayout?.setOnRefreshListener(OnRefreshListener { readEvents() })
        mSwipeRefreshLayout?.setOnRefreshListener(OnRefreshListener { readEvents() })
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (scrollView?.getScrollY()!! > 0) {
                        scrollView?.smoothScrollTo(0, 0)
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        readEvents()
        return view
    }

    private fun readEvents() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Events").child(
            firebaseUser?.uid.toString()
        )
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mEvents?.clear()
                for (snapshot in dataSnapshot.children) {

                    val eventModel = snapshot.getValue(MyEventsModel::class.java)
                    mEvents?.add(eventModel)
                }
                myEventsAdapter = MyEventsAdapter(context, mEvents)
                gridEvents?.adapter = myEventsAdapter
                Log.v(ContentValues.TAG, "mevent=" + mEvents)
                /** On liste tout les evenements  */
                placeEventsOnList()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun placeEventsOnList() {
        val activity: Activity? = activity
        if (isAdded && activity != null) {
            if (mEvents != null) {
                mSwipeRefreshLayout?.isRefreshing = false
                noResultsLayout?.isRefreshing = false
                noResultsLayout?.visibility = View.GONE
            } else {
                mProgressView?.visibility = View.GONE
                mSwipeRefreshLayout?.visibility = View.GONE
                noResultsLayout?.visibility = View.VISIBLE
                noResultsLayout?.isRefreshing = false
            }
        }
    }
}
