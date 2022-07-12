package com.etna.mycalendar.Fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.etna.mycalendar.Adapter.DisplayFoyerAdapter
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.ArrayList


class DisplayFoyerFragment
/** Constructeur de la classe DisplayUsersFragment  */
    : Fragment() {
    /** Déclaration variables  */
    private var filterButton: Button? = null
    private var scrollView: NestedScrollView? = null
    private var frameLayoutMap: FrameLayout? = null
    private var gridViewUsers: RecyclerView? = null
    private var displayFoyerAdapter: DisplayFoyerAdapter? = null
    private var mUsers: MutableList<UserModel?>? = null
    private var mUsersId: MutableList<String?>? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mProgressView: View? = null
    private var startRelativeLayout: LinearLayout? = null
    private var noResultsLayout: SwipeRefreshLayout? = null
    private var userIdToList: String? = null

    /**
     * onCreateView | utilisée à la place de onCreate. Pourquoi ? Nous sommes sur un fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_display_foyer, container, false)
        /** Initialisation des variables  */
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
        /** Contrôle sur le bouton 'Filtre'  */
        filterButton?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "Cette fonctionnalitée sera bientôt disponible ! Nos développeurs se concentre dessus",
                Toast.LENGTH_SHORT
            ).show()
        })
        noResultsLayout?.setOnRefreshListener(OnRefreshListener { _readUsers() })
        mSwipeRefreshLayout?.setOnRefreshListener(OnRefreshListener { _readUsers() })

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (scrollView?.getScrollY()!! > 0) {
                        scrollView?.smoothScrollTo(0, 0)
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        _readUsers()
        return view
    }

    private fun _readUsers() {
        mUsers!!.clear()
        mUsersId!!.clear()
        _showProgress(true)
        _getFromTable()
    }

    /**
     * Fonction permettant d'afficher les Utilisateurs
     */
    private fun _placeUsersOnList() {
        val activity: Activity? = activity
        if (isAdded && activity != null) {
            if (mUsers!!.size > 0) {
                _showProgress(false)
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
                ((if (show) 1 else 0.toFloat())).toFloat()
            ).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mSwipeRefreshLayout!!.visibility = if (show) View.GONE else View.VISIBLE
                }
            })
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                ((if (show) 1 else 0.toFloat())).toFloat()
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

    private fun _getFromTable() {
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
                _getToTable()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun _getToTable() {
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
                _getFinalUsers()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun _getFinalUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        /** Reference sur la table Users  */
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
                        _setUsersOnListAndAdapter()
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun _setUsersOnListAndAdapter() {
        gridViewUsers!!.layoutManager = GridLayoutManager(context, 2)
        displayFoyerAdapter = context?.let { DisplayFoyerAdapter(it, mUsers) }
        gridViewUsers!!.adapter = displayFoyerAdapter
        /** On place tout les Utilisateurs sur une liste  */
        _placeUsersOnList()
    }
}
