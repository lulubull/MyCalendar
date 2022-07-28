package com.etna.mycalendar.Fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.etna.mycalendar.Adapter.DisplayGroupeAdapter
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList

class DisplayGroupeFragment
    : Fragment() {
    private var filterButton: Button? = null
    private var scrollView: NestedScrollView? = null
    private var frameLayoutMap: FrameLayout? = null
    private var gridViewUsers: RecyclerView? = null
    private var displayGroupeAdapter: DisplayGroupeAdapter? = null
    private var mUsers: MutableList<UserModel?>? = null
    private var mUsersId: MutableList<String?>? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mProgressView: View? = null
    private var startRelativeLayout: LinearLayout? = null
    private var noResultsLayout: SwipeRefreshLayout? = null
    private var userIdToList: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_display_groupe, container, false)
        filterButton = view.findViewById(R.id.filterButton)
        scrollView = view.findViewById(R.id.scrollView)
        frameLayoutMap = view.findViewById(R.id.frameLayoutMap)
        noResultsLayout = view.findViewById(R.id.noResultsLayout)
        gridViewUsers = view.findViewById(R.id.listView)
        gridViewUsers?.setHasFixedSize(true)
        gridViewUsers?.setLayoutManager(LinearLayoutManager(context))
        gridViewUsers?.setNestedScrollingEnabled(false)
        startRelativeLayout = view.findViewById(R.id.startRelativeLayout)
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items)
        mProgressView = view.findViewById(R.id.progressBar)
        userIdToList = ""
        mUsers = ArrayList<UserModel?>()
        mUsersId = ArrayList()
        filterButton?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "Pas encore dev",
                Toast.LENGTH_SHORT
            ).show()
        })
        noResultsLayout?.setOnRefreshListener(OnRefreshListener { readUsers() })
        mSwipeRefreshLayout?.setOnRefreshListener(OnRefreshListener { readUsers() })
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (scrollView?.getScrollY()!! > 0) {
                        scrollView?.smoothScrollTo(0, 0)
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        readUsers()
        return view
    }

    private fun readUsers() {
        mUsers!!.clear()
        mUsersId!!.clear()
        getFromTable()
    }

    private fun placeUsersOnList() {
        val activity: Activity? = activity
        if (isAdded && activity != null) {
            if (mUsers!!.size > 0) {
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

    private fun getFromTable() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val referenceFrom = FirebaseDatabase.getInstance().getReference("Users").child(
            firebaseUser!!.uid
        ).child("requests").child("from")
        referenceFrom.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val request = snapshot.child("requestType").getValue(
                        String::class.java
                    )
                    if (request == "accepted") {
                        val userId = snapshot.key
                        mUsersId!!.add(userId)
                    }
                }
                getToTable()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getToTable() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val referenceTo = FirebaseDatabase.getInstance().getReference("Users").child(
            firebaseUser!!.uid
        ).child("requests").child("to")
        referenceTo.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val request = snapshot.child("requestType").getValue(
                        String::class.java
                    )
                    if (request == "accepted") {
                        val userId = snapshot.key
                        mUsersId!!.add(userId)
                    }
                }
                getFinalUsers()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getFinalUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val userModel: UserModel = snapshot.getValue(UserModel::class.java)!!
                    assert(firebaseUser != null)
                    for (usersId in mUsersId!!) {
                        if (userModel.id.equals(usersId)) {
                            mUsers!!.add(userModel)
                        }
                    }
                    if (mUsers!!.size == mUsersId!!.size) {
                        setUsersOnListAndAdapter()
                        break
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setUsersOnListAndAdapter() {
        gridViewUsers!!.layoutManager = GridLayoutManager(context, 2)
        displayGroupeAdapter = context?.let { DisplayGroupeAdapter(it, mUsers) }
        gridViewUsers!!.adapter = displayGroupeAdapter
        placeUsersOnList()
    }
}
