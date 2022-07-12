package com.etna.mycalendar.Fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
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
/** Constructeur de la classe EventsFragment  */
    : Fragment() {
    /** Déclaration variables  */
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
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        /** Initialisation des variables  */
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
        /** Contrôle sur le bouton 'Filtre'  */
        filterButton?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "Cette fonctionnalitée sera bientôt disponible ! Nos développeurs se concentre dessus",
                Toast.LENGTH_SHORT
            ).show()
        })
        addEventButton?.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, AddEventActivity::class.java)
            startActivity(intent)
        })
        noResultsLayout?.setOnRefreshListener(OnRefreshListener { _readEvents() })
        mSwipeRefreshLayout?.setOnRefreshListener(OnRefreshListener { _readEvents() })

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (scrollView?.getScrollY()!! > 0) {
                        scrollView?.smoothScrollTo(0, 0)
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        _readEvents()
        return view
    }

    /**
     * Fonction permettant de récuperer tout les Evenements de l'utilisateur
     */
    private fun _readEvents() {
        //_showProgress(true)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        /** Reference sur la table Events  */
        val reference = FirebaseDatabase.getInstance().getReference("Events").child(
            firebaseUser?.uid.toString()
        )
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mEvents?.clear()
                for (snapshot in dataSnapshot.children) {

                    val eventModel = snapshot.getValue(MyEventsModel::class.java)
                    Log.v(ContentValues.TAG, "les info user=" + eventModel)
                    mEvents?.add(eventModel)

                }
                myEventsAdapter = MyEventsAdapter(context, mEvents)
                gridEvents?.adapter = myEventsAdapter
                Log.v(ContentValues.TAG, "mevent=" + mEvents)
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
        val activity: Activity? = activity
        if (isAdded && activity != null) {
            if (mEvents != null) {
                //_showProgress(false)
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
