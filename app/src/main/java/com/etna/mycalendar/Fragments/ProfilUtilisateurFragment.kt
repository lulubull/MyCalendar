package com.etna.mycalendar.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.google.android.material.tabs.TabLayout
import java.util.ArrayList


class ProfilUtilisateurFragment(currentUserModel: UserModel) : Fragment() {
    /** Déclaration de variables  */
    private val currentUserModel: UserModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_menu, container, false)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        val viewPager: ViewPager = view.findViewById(R.id.view_pager)
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(ProfilFragment(currentUserModel), "Profil")
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        return view
    }

    internal inner class ViewPagerAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        init {
            fragments = ArrayList()
            titles = ArrayList()
        }
    }

    /** Constructeur  */
    init {
        this.currentUserModel = currentUserModel
    }
}
