package com.etna.mycalendar.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.Adapter.EventsRequestReceivedAdapter
import com.etna.mycalendar.Notifications.Token
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId

class EventsRequestReceivedFragment
    : Fragment() {
    private var gridViewEventsReceived: RecyclerView? = null
    private var requestReceivedAdapter: EventsRequestReceivedAdapter? = null
    private var mUserModels: MutableList<UserModel>? = null
    private var fuser: FirebaseUser? = null
    private var referenceRequests: DatabaseReference? = null
    private var keysList: MutableList<String>? = null
    private var scrollView: NestedScrollView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater.inflate(R.layout.fragment_events_request_received, container, false)
        gridViewEventsReceived = view.findViewById(R.id.listView)
        gridViewEventsReceived?.setHasFixedSize(true)
        gridViewEventsReceived?.setLayoutManager(LinearLayoutManager(context))
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items)
        scrollView = view.findViewById(R.id.scrollView)
        keysList = ArrayList()
        fuser = FirebaseAuth.getInstance().getCurrentUser()
        referenceRequests =
            FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.getUid())
                .child("requests").child("from")
        referenceRequests!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (keysList as ArrayList<String>).clear()
                for (snapshot in dataSnapshot.getChildren()) {
                    val requestType: String = snapshot.child("requestType").getValue<String>(
                        String::class.java
                    ).toString()
                    val key: String = snapshot.getKey().toString()
                    (keysList as ArrayList<String>).add(snapshot.getKey().toString())
                }
                readReceivedRequests()
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        mSwipeRefreshLayout?.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                readReceivedRequests()
            }
        })
        // callback recall last frag (btn back)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (scrollView?.getScrollY()!! > 0) {
                        scrollView?.smoothScrollTo(0, 0)
                    }
                }
            }
        updateToken(FirebaseInstanceId.getInstance().getToken().toString())
        return view
    }

    private fun updateToken(token: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token)
        reference.child(fuser?.getUid().toString()).setValue(token1)
    }

    private fun readReceivedRequests() {
        mUserModels = ArrayList<UserModel>()
        val referenceUser: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        referenceUser.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUserModels!!.clear()
                for (snapshot in dataSnapshot.getChildren()) {
                    val userModel: UserModel = snapshot.getValue(UserModel::class.java)!!
                    for (key in keysList!!) {
                        if (userModel.id.equals(key)) {
                            Log.d("userId", userModel.id.toString())
                            mUserModels!!.add(userModel)
                        }
                    }
                }
                requestReceivedAdapter = EventsRequestReceivedAdapter(context, mUserModels )
                gridViewEventsReceived?.setAdapter(requestReceivedAdapter)
                mSwipeRefreshLayout?.setRefreshing(false)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}