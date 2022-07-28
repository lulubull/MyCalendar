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
import com.etna.mycalendar.Adapter.EventsRequestSendedAdapter
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.ArrayList

class EventsRequestSendedFragment : Fragment() {
    private var listViewRequestsSended: RecyclerView? = null
    private var requestSendedAdapter: EventsRequestSendedAdapter? = null
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
        val view: View = inflater.inflate(R.layout.fragment_events_request_sended, container, false)
        listViewRequestsSended = view.findViewById(R.id.listView)
        listViewRequestsSended?.setHasFixedSize(true)
        listViewRequestsSended?.setLayoutManager(LinearLayoutManager(context))
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items)
        scrollView = view.findViewById(R.id.scrollView)
        keysList = ArrayList()
        fuser = FirebaseAuth.getInstance().getCurrentUser()
        referenceRequests =
            FirebaseDatabase.getInstance().getReference("Users").child(fuser?.getUid().toString())
                .child("requests").child("to")
        referenceRequests!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (keysList as ArrayList<String>).clear()
                for (snapshot in dataSnapshot.getChildren()) {
                    (keysList as ArrayList<String>).add(snapshot.getKey().toString())
                }
                readSendedRequests()
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        mSwipeRefreshLayout?.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                readSendedRequests()
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
        return view
    }

    /**
     * get frends rquest and push to adapter
     */
    private fun readSendedRequests() {
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
                requestSendedAdapter = EventsRequestSendedAdapter(context, mUserModels)
                listViewRequestsSended?.setAdapter(requestSendedAdapter)
                mSwipeRefreshLayout?.setRefreshing(false)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}