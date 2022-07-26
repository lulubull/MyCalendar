package com.etna.mycalendar.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.etna.mycalendar.Adapter.UserAdapter
import com.etna.mycalendar.Models.ChatListModel
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.Notifications.Token
import com.etna.mycalendar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import java.util.ArrayList

class ChatsFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUserModels: MutableList<UserModel?>? = null
    private var search_users: EditText? = null
    private var searchUserParced: String? = null
    private var fuser: FirebaseUser? = null
    private var reference: DatabaseReference? = null
    private var usersList: MutableList<ChatListModel>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mUserModels = savedInstanceState.getParcelableArrayList<UserModel>("mUserModels")
            Log.d("mUserModels", mUserModels.toString())
            Log.d("savedInstanceNull", "savedInstanceNull")
            searchUserParced = savedInstanceState.getString("searchUserParced")
        } else {
            mUserModels = ArrayList<UserModel?>()
            Log.d("mUserModels", mUserModels.toString())
            Log.d("savedInstanceOk", "savedInstanceOk")
            searchUserParced = ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_chats, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.setLayoutManager(LinearLayoutManager(context))
        search_users = view.findViewById<EditText>(R.id.search_users)
        fuser = FirebaseAuth.getInstance().getCurrentUser()
        usersList = ArrayList<ChatListModel>()
        search_users?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                _searchUsers(charSequence.toString().toLowerCase())
                searchUserParced = charSequence.toString().toLowerCase()
            }

            override fun afterTextChanged(editable: Editable) {
                _searchUsers(searchUserParced)
            }
        })
        _getUserChatList()
        _updateToken(FirebaseInstanceId.getInstance().getToken().toString())
        return view
    }

    private fun _updateToken(token: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token)
        reference.child(fuser?.getUid().toString()).setValue(token1)
    }

    private fun _chatList() {
        mUserModels = ArrayList<UserModel?>()
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUserModels!!.clear()
                for (snapshot in dataSnapshot.getChildren()) {
                    val userModel: UserModel? = snapshot.getValue(UserModel::class.java)
                    for (chatListModel in usersList!!) {
                        if (userModel?.id.equals(chatListModel.id)) {
                            mUserModels!!.add(userModel)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, mUserModels, true)
                recyclerView?.setAdapter(userAdapter)
                _updateToken(FirebaseInstanceId.getInstance().getToken().toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun _searchUsers(s: String?) {
        val query: Query =
            FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s + "\uf8ff")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUserModels!!.clear()
                for (snapshot in dataSnapshot.getChildren()) {
                    val userModel: UserModel? = snapshot.getValue(UserModel::class.java)
                    for (chatListModel in usersList!!) {
                        if (userModel?.id.equals(chatListModel.id)) {
                            mUserModels!!.add(userModel)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, mUserModels, false)
                recyclerView?.setAdapter(userAdapter)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun _getUserChatList() {
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser?.getUid().toString())
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersList!!.clear()
                for (snapshot in dataSnapshot.getChildren()) {
                    val chatListModel: ChatListModel? = snapshot.getValue(ChatListModel::class.java)
                    usersList!!.add(chatListModel!!)
                }
                _chatList()
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchUserParced", searchUserParced)
        outState.putParcelableArrayList("mUserModels", mUserModels as ArrayList<UserModel?>?)
    }
}