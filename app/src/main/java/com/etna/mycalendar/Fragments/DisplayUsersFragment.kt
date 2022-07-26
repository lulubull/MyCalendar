package com.etna.mycalendar.Fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.etna.mycalendar.Activity.UserProfilViewActivity
import com.etna.mycalendar.Adapter.DisplayUsersAdapter
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException
import java.util.ArrayList

class DisplayUsersFragment : Fragment() {
    private var aroundMe: Button? = null
    private var filterButton: Button? = null
    private var scrollView: NestedScrollView? = null
    private var frameLayoutMap: FrameLayout? = null
    private var gridViewUsers: RecyclerView? = null
    private var displayUsersAdapter: DisplayUsersAdapter? = null
    private var mUsers: MutableList<UserModel?>? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mProgressView: View? = null
    private var startRelativeLayout: LinearLayout? = null
    private var noResultsLayout: SwipeRefreshLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_display_users, container, false)
        aroundMe = view.findViewById(R.id.aroundMe)
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
        mUsers = ArrayList<UserModel?>()
        aroundMe?.setOnClickListener(
            View.OnClickListener {
                if (aroundMe?.getText().toString() == "Carte") {
                    aroundMe?.setText("Liste")
                    if (mUsers?.size!! > 0) {
                        noResultsLayout?.setVisibility(View.GONE)
                        mSwipeRefreshLayout?.setVisibility(View.GONE)
                        frameLayoutMap?.setVisibility(View.VISIBLE)
                    } else {
                        noResultsLayout?.setVisibility(View.VISIBLE)
                        mSwipeRefreshLayout?.setVisibility(View.GONE)
                        frameLayoutMap?.setVisibility(View.GONE)
                    }
                } else if (aroundMe?.getText().toString() == "Liste") {
                    aroundMe?.setText("Carte")
                    if (mUsers!!.size > 0) {
                        noResultsLayout?.setVisibility(View.GONE)
                        mSwipeRefreshLayout?.setVisibility(View.VISIBLE)
                        frameLayoutMap?.setVisibility(View.GONE)
                    } else {
                        noResultsLayout?.setVisibility(View.VISIBLE)
                        mSwipeRefreshLayout?.setVisibility(View.GONE)
                        frameLayoutMap?.setVisibility(View.GONE)
                    }
                }
            })
        filterButton?.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                context,
                "Cette fonctionnalitée sera bientôt disponible ! Nos développeurs se concentre dessus",
                Toast.LENGTH_SHORT
            ).show()
        })
        noResultsLayout?.setOnRefreshListener(OnRefreshListener { _readUsers() })
        mSwipeRefreshLayout?.setOnRefreshListener(OnRefreshListener { _readUsers() })

        // callback for last frag used
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (scrollView?.getScrollY()!! > 0) {
                        scrollView?.smoothScrollTo(0, 0)
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        _readUsers()
        return view
    }

    private fun _readUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers?.clear()
                for (snapshot in dataSnapshot.children) {
                    val userModel: UserModel = snapshot.getValue(UserModel::class.java)!!
                    assert(firebaseUser != null)
                    if (!userModel.id.equals(firebaseUser?.uid)) {
                        mUsers?.add(userModel)
                    }
                }
                gridViewUsers?.layoutManager = GridLayoutManager(context, 2)
                DisplayUsersAdapter(context, mUsers).also { displayUsersAdapter = it }
                gridViewUsers?.adapter = displayUsersAdapter
                _placeMarkersOnMap()
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun _placeMarkersOnMap() {
        val activity: Activity? = activity
        if (isAdded && activity != null) {
            if (mUsers!!.size > 0) {
                val mapFragment: SupportMapFragment? =
                    childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync(object : OnMapReadyCallback {
                    override fun onMapReady(mMap: GoogleMap) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
                        mMap.clear() //clear old markers
                        val googlePlex: CameraPosition = CameraPosition.builder()
                            .target(LatLng(47.155404566149336, 2.6491201250000813))
                            .zoom(2F)
                            .bearing(0F)
                            .tilt(45F)
                            .build()
                        mMap.animateCamera(
                            CameraUpdateFactory.newCameraPosition(googlePlex),
                            10000,
                            null
                        )
                        for (usr in mUsers!!) {
                            val adress: String = usr?.numeroVoie
                                .toString() + " " + usr?.typeVoie + " " + usr?.nomVoie + "," + usr?.codePostal
                            val adressLatLng: LatLng? =
                                getLocationFromAddress(getActivity(), adress)
                            if (adressLatLng != null) {
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(
                                            LatLng(
                                                adressLatLng.latitude,
                                                adressLatLng.longitude
                                            )
                                        )
                                        .title(usr?.id)
                                        .icon(
                                            bitmapDescriptorFromVector(
                                                getActivity(),
                                                R.drawable.ic_person_black_24dp
                                            )
                                        )
                                )
                            }
                        }
                        mSwipeRefreshLayout?.isRefreshing = false
                        noResultsLayout?.isRefreshing = false
                        noResultsLayout?.visibility = View.GONE
                        aroundMe?.text = "Carte"
                        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
                            override fun onMarkerClick(marker: Marker): Boolean {
                                val intent =
                                    Intent(getActivity(), UserProfilViewActivity::class.java)
                                intent.putExtra("idUser", marker.getTitle())
                                startActivity(intent)
                                return true
                            }
                        })
                    }
                })
            } else {
                mProgressView!!.visibility = View.GONE
                mSwipeRefreshLayout!!.visibility = View.GONE
                noResultsLayout!!.visibility = View.VISIBLE
                noResultsLayout!!.isRefreshing = false
            }
        }
    }

    fun getLocationFromAddress(context: Context?, strAddress: String?): LatLng? {
        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null
        try {
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            val location = address[0]
            p1 = LatLng(location.latitude, location.longitude)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return p1
    }

    private fun bitmapDescriptorFromVector(context: Context?, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(requireContext(), vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * print loader
    */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun _showProgress(show: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)
            mSwipeRefreshLayout!!.visibility = if (show) View.GONE else View.VISIBLE
            mSwipeRefreshLayout!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                ((if (show) 0 else 1.toInt()) as Int).toFloat()
            ).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mSwipeRefreshLayout!!.visibility = if (show) View.GONE else View.VISIBLE
                }
            })
            mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                ((if (show) 0 else 1.toInt()) as Int).toFloat()
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
