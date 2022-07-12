package com.etna.mycalendar.Activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.etna.mycalendar.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.etna.mycalendar.Models.MyEventsModel
import com.etna.mycalendar.Models.UserModel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*


class AddEventActivity : AppCompatActivity() {

    /** Déclaration de variables  */
    var startDate: EditText? = null
    /** Déclaration de variables  */
    private var endDate: EditText? = null
    private var startHour: EditText? = null
    private var endHour:EditText? = null
    private var eventDescription: EditText? = null
    private var btnAddEvent: Button? = null
    private var btnAddUsers:android.widget.Button? = null
    private lateinit var booleanTab: BooleanArray
    private var mUsers: MutableList<UserModel?>? = null
    private var mUsersId: MutableList<String?>? = null
    private var hashMap: HashMap<String, String>? = null
    private var selectedItems: ArrayList<Any?>? = null
    private var date: String? = null
    private var idUserBundle: String? = null
    private var startDateString: String? = null
    private var endDateString:kotlin.String? = null
    private var startHourString:kotlin.String? = null
    private var endHourString:kotlin.String? = null
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var myAlertDialog: AlertDialog? = null
    private val currentUser = FirebaseAuth.getInstance().currentUser


    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        /** Animation Transition  */
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
        /** Mise en place de la toolbar  */

        getSupportActionBar()?.setDisplayShowTitleEnabled(false)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        /** Initialisation des variables  */
        selectedItems = ArrayList<Any?>()
        mUsers = ArrayList<UserModel?>()
        mUsersId = ArrayList<String?>()
        hashMap = HashMap()
        idUserBundle = getIntent().getSerializableExtra("idUser") as String?
        startDate = findViewById<EditText>(R.id.startDate)
        endDate = findViewById<EditText>(R.id.endDate)
        startHour = findViewById<EditText>(R.id.startHour)
        endHour = findViewById<EditText>(R.id.endHour)
        eventDescription = findViewById<EditText>(R.id.eventDescription)
        btnAddEvent = findViewById<Button>(R.id.btnAddEvent)
        btnAddUsers = findViewById<Button>(R.id.btnAddUsers)
        if (idUserBundle != null) {
            btnAddUsers?.setVisibility(View.GONE)
        }
        startDate?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val mcurrentDate = Calendar.getInstance()
                Locale.setDefault(Locale.FRANCE)
                mDay = mcurrentDate[Calendar.DAY_OF_MONTH]
                mMonth = mcurrentDate[Calendar.MONTH]
                mYear = mcurrentDate[Calendar.YEAR]
                val datePickerDialog =
                    DatePickerDialog(this@AddEventActivity, object : OnDateSetListener {
                        override fun onDateSet(
                            view: DatePicker,
                            year: Int,
                            month: Int,
                            dayOfMonth: Int
                        ) {
                            var month = month
                            month = month + 1
                            date = "$dayOfMonth/$month/$year"
                            startDateString = date
                            startDate?.setText(date)
                        }
                    }, mYear, mMonth, mDay)
                datePickerDialog.show()
            }
        })
        endDate?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val mcurrentDate = Calendar.getInstance()
                Locale.setDefault(Locale.FRANCE)
                mDay = mcurrentDate[Calendar.DAY_OF_MONTH]
                mMonth = mcurrentDate[Calendar.MONTH]
                mYear = mcurrentDate[Calendar.YEAR]
                val datePickerDialog =
                    DatePickerDialog(this@AddEventActivity, object : OnDateSetListener {
                        override fun onDateSet(
                            view: DatePicker,
                            year: Int,
                            month: Int,
                            dayOfMonth: Int
                        ) {
                            var month = month
                            month = month + 1
                            date = "$dayOfMonth/$month/$year"
                            endDateString = date
                            endDate?.setText(date)
                        }
                    }, mYear, mMonth, mDay)
                datePickerDialog.show()
            }
        })
        startHour?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val mcurrentTime = Calendar.getInstance()
                Locale.setDefault(Locale.FRANCE)
                val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
                val minute = mcurrentTime[Calendar.MINUTE]
                val mTimePicker: TimePickerDialog
                mTimePicker = TimePickerDialog(this@AddEventActivity, object : OnTimeSetListener {
                    override fun onTimeSet(
                        timePicker: TimePicker,
                        selectedHour: Int,
                        selectedMinute: Int
                    ) {
                        startHourString = "$selectedHour:$selectedMinute"
                        startHour?.setText("$selectedHour:$selectedMinute")
                    }
                }, hour, minute, true)
                mTimePicker.setTitle("Select Time")
                mTimePicker.show()
            }
        })
        endHour?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val mcurrentTime = Calendar.getInstance()
                Locale.setDefault(Locale.FRANCE)
                val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
                val minute = mcurrentTime[Calendar.MINUTE]
                val mTimePicker: TimePickerDialog
                mTimePicker = TimePickerDialog(this@AddEventActivity, object : OnTimeSetListener {
                    override fun onTimeSet(
                        timePicker: TimePicker,
                        selectedHour: Int,
                        selectedMinute: Int
                    ) {
                        endHourString = "$selectedHour:$selectedMinute"
                        endHour?.setText("$selectedHour:$selectedMinute")
                    }
                }, hour, minute, true)
                mTimePicker.setTitle("Select Time")
                mTimePicker.show()
            }
        })
        btnAddUsers?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                // _launchDialog();
                _getFromTable()
            }
        })
        btnAddEvent?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (startDateString == "" || endDateString == "" || startHourString == "" || endHourString == "" || eventDescription == null) {
                    val toast = Toast.makeText(
                        getApplicationContext(),
                        "Veuillez remplir tout les champs",
                        Toast.LENGTH_SHORT
                    )
                } else {
                    val database = FirebaseDatabase.getInstance()
                    val myEventsDb: DatabaseReference
                    val myEventsCurrentUser: DatabaseReference
                    val myEventsDestinataire: DatabaseReference
                    val mGroupId: String?
                    if (idUserBundle == null) {
                        myEventsDb = database.getReference("Events").child(currentUser?.getUid().toString())
                        myEventsDestinataire = database.getReference("Events")
                        mGroupId = myEventsDb.push().key
                        if (hashMap!!.size > 0 && hashMap != null) {
                            var id: String? = ""
                            hashMap!!["500"] = currentUser?.getUid().toString()
                            for (i in 0 until hashMap!!.size) {
                                id = if (i == hashMap!!.size - 1) {
                                    hashMap!!.get("500")
                                } else {
                                    hashMap!!.get(Integer.toString(i))
                                }
                                myEventsDestinataire.child(id.toString()).child(mGroupId.toString()).setValue(
                                    MyEventsModel(
                                        mGroupId,
                                        startDateString.toString(),
                                        endDateString.toString(),
                                        startHourString.toString(),
                                        endHourString.toString(),
                                        eventDescription?.getText().toString()
                                    )
                                )
                                myEventsDestinataire.child(id.toString()).child(mGroupId.toString()).child("sharedWith")
                                    .setValue(hashMap)
                            }
                            // myEventsDb.child(mGroupId).child("sharedWith").setValue(hashMap);
                        } else {
                            myEventsDb.child(mGroupId!!).setValue(
                                MyEventsModel(
                                    mGroupId,
                                    startDateString.toString(),
                                    endDateString.toString(),
                                    startHourString.toString(),
                                    endHourString.toString(),
                                    eventDescription?.getText().toString()
                                )
                            )
                        }
                    } else {
                        val hashMapFinalDb = HashMap<String, String>()
                        val hashMapFinalUser = HashMap<String, String>()
                        hashMapFinalDb["0"] = idUserBundle!!
                        hashMapFinalUser["0"] = currentUser?.getUid().toString()
                        myEventsDb = database.getReference("Events").child(idUserBundle!!)
                        myEventsCurrentUser =
                            database.getReference("Events").child(currentUser?.getUid().toString())
                        mGroupId = myEventsDb.push().key
                        myEventsDb.child(mGroupId.toString()).setValue(
                            MyEventsModel(
                                mGroupId,
                                startDateString.toString(),
                                endDateString.toString(),
                                startHourString.toString(),
                                endHourString.toString(),
                                eventDescription?.getText().toString()

                            )
                        )
                        myEventsCurrentUser.child(mGroupId.toString()).setValue(
                            MyEventsModel(
                                mGroupId,
                                startDateString.toString(),
                                endDateString.toString(),
                                startHourString.toString(),
                                endHourString.toString(),
                                eventDescription?.getText().toString()

                            )
                        )
                        myEventsDb.child(mGroupId.toString()).child("sharedWith").setValue(hashMapFinalUser)
                        myEventsCurrentUser.child(mGroupId.toString()).child("sharedWith")
                            .setValue(hashMapFinalDb)
                    }
                    val intent = Intent(this@AddEventActivity, MainActivity::class.java)
                    startActivity(intent)
                    val toast = Toast.makeText(
                        getApplicationContext(),
                        "Evenement ajouté !",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }
        })
    }

    /**
     *
     * @return
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Permet de récuperer l'ID des utilisateurs (String) 'ami' avec l'utilisateur
     * A partir du statut des DEMANDES ENVOYÉES PAR L'UTILISATEUR COURANT
     * Utilisé pour l'ouverture du Dialog permettant de sélectionner les utilisateurs avec qui partager l'évenement
     */
    private fun _getFromTable() {
        mUsersId?.clear()
        mUsers?.clear()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val referenceFrom =
            FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser?.getUid().toString())
                .child("requests").child("from")
        referenceFrom.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val request = snapshot.child("requestType").getValue(
                        String::class.java
                    )
                    if (request == "accepted") {
                        val userId = snapshot.key
                        mUsersId?.add(userId)
                    }
                }
                _getToTable()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    /**
     * Permet de récuperer l'ID des utilisateurs (String) 'ami' avec l'utilisateur
     * A partir du statut des DEMANDES ENVOYÉES VERS L'UTILISATEUR COURANT
     * Utilisé pour l'ouverture du Dialog permettant de sélectionner les utilisateurs avec qui partager l'évenement
     */
    private fun _getToTable() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val referenceTo =
            FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser?.getUid().toString())
                .child("requests").child("to")
        referenceTo.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val request = snapshot.child("requestType").getValue(
                        String::class.java
                    )
                    if (request == "accepted") {
                        val userId = snapshot.key
                        mUsersId?.add(userId)
                    }
                }
                _getFinalUsers()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    /**
     * Permet de récuperer les utilisateurs (UserModel) 'ami' avec l'utilisateur
     * A partir du statut des DEMANDES ENVOYÉES VERS L'UTILISATEUR COURANT
     * Utilisé pour l'ouverture du Dialog permettant de sélectionner les utilisateurs avec qui partager l'évenement
     */
    private fun _getFinalUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        /** Reference sur la table Users  */
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val userModel = snapshot.getValue(UserModel::class.java)!!
                    assert(firebaseUser != null)
                    for (usersId in mUsersId!!) {
                        if (userModel.id.equals(usersId)) {
                            mUsers?.add(userModel)
                        }
                    }
                    if (mUsers?.size == mUsersId!!.size) {
                        _launchDialog()
                        break
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

     fun _launchDialog() {
        hashMap?.clear()
        val addUsersBuilder = AlertDialog.Builder(this@AddEventActivity)

        // On se base sur la liste des utilisateurs "amis"
        val charSequencesId = arrayOf<CharSequence>(mUsers!!.size.toString())
        booleanTab = BooleanArray(mUsers!!.size)

        // On initialise la valeur des utilisateur à ajouter (username)
        for (i in mUsers!!.indices) {
            charSequencesId[i] = mUsers!!.get(i)?.username.toString()
        }
        addUsersBuilder.setTitle("Ajouter un utilisateur")
            .setMultiChoiceItems(charSequencesId, booleanTab, object : OnMultiChoiceClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int, isChecked: Boolean) {
                    // Gestion des click des checkbox
                    if (isChecked) {
                        selectedItems?.add(charSequencesId[i] as String)
                        // booleanTab[i] = true;
                    } else if (selectedItems!!.contains(i)) {
                        selectedItems?.removeAt(Integer.valueOf(i))
                    }
                }
            })
        addUsersBuilder.setPositiveButton(
            "Ajouter à l'événement",
            object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface, i: Int) {
                    // On parcourt la liste des logins selectionnés (checkbox)
                    for (j in selectedItems!!.indices) {
                        // On parcourt la liste des utilisteurs "amis"
                        for (k in mUsers!!.indices) {
                            // Sachant qu'il ne peut pas y avoir 2 logins identiques
                            // Si le login selectionné correspond au login de l'utilisateur "ami"
                            // on ajoute son id dans hashMap
                            if (selectedItems!!.get(j).toString() == mUsers!!.get(k)?.username) {
                                hashMap!!.set(Integer.toString(j), mUsers?.get(k)?.id.toString())
                            }
                        }
                    }
                }
            })
        myAlertDialog = addUsersBuilder.create()
        myAlertDialog?.show()
    }
}
