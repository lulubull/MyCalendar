package com.etna.mycalendar.Activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.etna.mycalendar.Models.UserModel
import com.etna.mycalendar.R
import com.etna.mycalendar.Models.MyEventsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class AddEventActivity : AppCompatActivity() {
    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var startHour: EditText
    private lateinit var endHour: EditText
    private lateinit var eventDescription: EditText
    private lateinit var btnAddEvent: Button
    private lateinit var btnAddUsers: Button
    private lateinit var booleanTab: BooleanArray
    private lateinit var mUsers: MutableList<UserModel?>
    private lateinit var mUsersId: MutableList<String?>
    private lateinit var hashMap: HashMap<String, String>
    private lateinit var selectedItems: ArrayList<Any>
    private lateinit var date: String
    private lateinit var idUserBundle: String
    private lateinit var startDateString: String
    private lateinit var startHourString: String
    private lateinit var endHourString: String
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private lateinit var endDateString: String
    private lateinit var myAlertDialog: AlertDialog
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        selectedItems = ArrayList<Any>()
        mUsers = ArrayList()
        mUsersId = ArrayList()
        hashMap = HashMap<String, String>()
        idUserBundle = (intent.getSerializableExtra("idUser").toString())
        startDate = findViewById(R.id.startDate)
        endDate = findViewById(R.id.endDate)
        startHour = findViewById(R.id.startHour)
        endHour = findViewById(R.id.endHour)
        eventDescription = findViewById(R.id.eventDescription)
        btnAddEvent = findViewById(R.id.btnAddEvent)
        btnAddUsers = findViewById(R.id.btnAddUsers)

        startDate?.setOnClickListener(View.OnClickListener {
            val mcurrentDate = Calendar.getInstance()
            Locale.setDefault(Locale.FRANCE)
            mDay = mcurrentDate[Calendar.DAY_OF_MONTH]
            mMonth = mcurrentDate[Calendar.MONTH]
            mYear = mcurrentDate[Calendar.YEAR]
            val datePickerDialog = DatePickerDialog(this@AddEventActivity,
                { view, year, month, dayOfMonth ->
                    var month = month
                    month += 1
                    date = "$dayOfMonth/$month/$year"
                    startDateString = date
                    startDate?.setText(date)
                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
        })

        endDate?.setOnClickListener(View.OnClickListener {
            val mcurrentDate = Calendar.getInstance()
            Locale.setDefault(Locale.FRANCE)
            mDay = mcurrentDate[Calendar.DAY_OF_MONTH]
            mMonth = mcurrentDate[Calendar.MONTH]
            mYear = mcurrentDate[Calendar.YEAR]
            val datePickerDialog = DatePickerDialog(this@AddEventActivity,
                { view, year, month, dayOfMonth ->
                    var month = month
                    month += 1
                    date = "$dayOfMonth/$month/$year"
                    endDateString = date
                    endDate?.setText(date)
                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
        })

        startHour?.setOnClickListener(View.OnClickListener {
            val mcurrentTime = Calendar.getInstance()
            Locale.setDefault(Locale.FRANCE)
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this@AddEventActivity,
                { timePicker, selectedHour, selectedMinute ->
                    startHourString = "$selectedHour:$selectedMinute"
                    startHour?.setText("$selectedHour:$selectedMinute")
                }, hour, minute, true
            )
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        })

        endHour?.setOnClickListener(View.OnClickListener {
            val mcurrentTime = Calendar.getInstance()
            Locale.setDefault(Locale.FRANCE)
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog = TimePickerDialog(this@AddEventActivity,
                { timePicker, selectedHour, selectedMinute ->
                    endHourString = "$selectedHour:$selectedMinute"
                    endHour?.setText("$selectedHour:$selectedMinute")
                }, hour, minute, true
            )
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        })

        btnAddUsers?.setOnClickListener(View.OnClickListener {
            _getFromTable()
        })

        btnAddEvent?.setOnClickListener(View.OnClickListener {
            if (startDateString == "" || endDateString == "" || startHourString == "" || endHourString == "" || eventDescription.toString() == "") {
                val toast = Toast.makeText(
                    applicationContext,
                    "Veuillez remplir tout les champs",
                    Toast.LENGTH_SHORT
                )
            } else {
                val database = FirebaseDatabase.getInstance()
                val myEventsDb: DatabaseReference
                val myEventsCurrentUser: DatabaseReference
                val myEventsDestinataire: DatabaseReference
                val mGroupId: String?
                if (idUserBundle != null) {
                    myEventsDb = database.getReference("Events").child(currentUser?.uid.toString())
                    myEventsDestinataire = database.getReference("Events")
                    mGroupId = myEventsDb.push().key
                    if (hashMap?.size > 0 && hashMap != null) {
                        var id: String? = ""
                        hashMap["500"] = currentUser?.uid.toString()
                        for (i in 0 until hashMap?.size) {
                            id = if (i == hashMap?.size - 1) {
                                hashMap["500"]
                            } else {
                                hashMap[Integer.toString(i)]
                            }
                            myEventsDestinataire.child(id.toString()).child(mGroupId.toString()).setValue(
                                MyEventsModel(
                                    null,
                                    startDateString,
                                    endDateString,
                                    startHourString,
                                    endHourString,
                                    eventDescription?.getText().toString(),
                                null
                                )
                            )
                            myEventsDestinataire.child(id.toString()).child(mGroupId.toString()).child("sharedWith")
                                .setValue(hashMap)
                        }
                    } else {
                        myEventsDb.child(mGroupId.toString()).setValue(
                            MyEventsModel(
                                null,
                                startDateString,
                                endDateString,
                                startHourString,
                                endHourString,
                                eventDescription?.getText().toString(),
                                null
                            )
                        )
                    }
                } else {
                    val hashMapFinalDb = HashMap<String, String>()
                    val hashMapFinalUser = HashMap<String, String>()
                    hashMapFinalDb["0"] = idUserBundle
                    hashMapFinalUser["0"] = currentUser!!.uid
                    myEventsDb = database.getReference("Events").child(idUserBundle)
                    myEventsCurrentUser = database.getReference("Events").child(currentUser.uid)
                    mGroupId = myEventsDb.push().key
                    myEventsDb.child(mGroupId.toString()).setValue(
                        MyEventsModel(
                            null,
                            startDateString,
                            endDateString,
                            startHourString,
                            endHourString,
                            eventDescription?.getText().toString(),
                            null
                        )
                    )
                    myEventsCurrentUser.child(mGroupId.toString()).setValue(
                        MyEventsModel(
                            null,
                            startDateString,
                            endDateString,
                            startHourString,
                            endHourString,
                            eventDescription?.getText().toString(),
                            null
                        )
                    )
                    myEventsDb.child(mGroupId.toString()).child("sharedWith").setValue(hashMapFinalUser)
                    myEventsCurrentUser.child(mGroupId.toString()).child("sharedWith").setValue(hashMapFinalDb)
                }

                val intent = Intent(this@AddEventActivity, MainActivity::class.java)
                startActivity(intent)
                val toast =
                    Toast.makeText(applicationContext, "Evenement créé", Toast.LENGTH_SHORT)
                toast.show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * get frend's id with current user
     * from current user request
     */
    private fun _getFromTable() {
        mUsersId!!.clear()
        mUsers!!.clear()
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

    /**
     * get frend's id with users
     * aplies to users
     */
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

    /**
     * Get frends model User with current user
     * aplies to current user
     */
    private fun _getFinalUsers() {
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
                        _launchDialog()
                        break
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun _launchDialog() {
        hashMap!!.clear()
        val addUsersBuilder = AlertDialog.Builder(this@AddEventActivity)
        // lst frend
        val charSequencesId = arrayOfNulls<CharSequence>(
            mUsers!!.size
        )
        booleanTab = BooleanArray(mUsers!!.size)
        for (i in mUsers!!.indices) {
            charSequencesId[i] = mUsers.get(i)?.username
        }
        addUsersBuilder.setTitle("Ajouter un utilisateur").setMultiChoiceItems(
            charSequencesId, booleanTab
        ) { dialogInterface, i, isChecked ->
            if (isChecked) {
                selectedItems?.add(charSequencesId[i]!!)
            } else if (selectedItems!!.contains(i)) {
                selectedItems!!.removeAt(Integer.valueOf(i))
            }
        }
        addUsersBuilder.setPositiveButton(
            "Ajouter à l'utilisateur à l'événement"
        ) { dialogInterface, i ->
            // on users choise
            for (j in selectedItems!!.indices) {
                // lst frend
                for (k in mUsers!!.indices) {
                    if (selectedItems!![j].toString() == mUsers!![k]?.username) {
                        hashMap!![Integer.toString(j)] = mUsers!![k]?.id.toString()
                    }
                }
            }
        }
        myAlertDialog = addUsersBuilder.create()
        myAlertDialog?.show()
    }
}
