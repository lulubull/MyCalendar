package com.etna.mycalendar.Activity

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.etna.mycalendar.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import java.util.regex.Pattern
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private var date: String? = null
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        birthDate.setOnClickListener(
            View.OnClickListener {
                val mcurrentDate = Calendar.getInstance()
                Locale.setDefault(Locale.FRANCE)
                mDay = mcurrentDate[Calendar.DAY_OF_MONTH]
                mMonth = mcurrentDate[Calendar.MONTH]
                mYear = mcurrentDate[Calendar.YEAR]
                val datePickerDialog = DatePickerDialog(this@RegisterActivity,
                    { view, year, month, dayOfMonth ->
                        var month = month
                        month += 1
                        date = "$dayOfMonth:$month:$year"
                        birthDate.setText(date)
                    }, mYear, mMonth, mDay
                )
                datePickerDialog.show()
            })
        val adressTypeSpinner = findViewById<Spinner>(R.id.adressTypeSpinner)
        val adresseTypeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.adressTypeSpinner, android.R.layout.simple_spinner_item
        )
        adresseTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adressTypeSpinner.adapter = adresseTypeAdapter
        adressTypeSpinner.setOnItemSelectedListener(this)
        btn_register.setOnClickListener(View.OnClickListener {
            val upperCase = Pattern.compile("[A-Z]")
            val lowerCase = Pattern.compile("[a-z]")
            val digitCase = Pattern.compile("[0-9]")
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            val txt_username: String = username.getText().toString()
            val txt_email: String = email.getText().toString()
            val txt_lastname = lastname.getText().toString()
            val txt_firstname: String = firstname.getText().toString()
            val txt_birthdate: String = birthDate.getText().toString()
            val txt_nbVoie: String = numeroVoieET.getText().toString()
            val txt_typeVoie = adressTypeSpinner.getSelectedItem().toString()
            val txt_nomVoie: String = adressNameET.getText().toString()
            val txt_codePostal: String = codePostal.getText().toString()
            val txt_townName: String = townName.getText().toString()
            val txt_telephone: String = numeroTelephoneET.getText().toString()
            val txt_password: String = passwordTxt.getText().toString()
            val txt_passwordRetype = passwordTxtRetype.getText().toString()
            _registrationProcess(
                txt_username,
                txt_email,
                txt_password,
                txt_lastname,
                txt_firstname,
                txt_birthdate,
                txt_nbVoie,
                txt_typeVoie,
                txt_nomVoie,
                txt_codePostal,
                txt_townName,
                txt_telephone
            )
        })
    }

    private fun _registrationProcess(
        username: String,
        email: String,
        password: String,
        txt_lastname: String,
        txt_firstname: String,
        txt_birthdate: String,
        txt_nbVoie: String,
        txt_typeVoie: String,
        txt_nomVoie: String,
        txt_codePostal: String,
        ville: String,
        telephone: String
    ) {
        loader_layout.visibility = View.VISIBLE
        scroll_register_view!!.visibility = View.GONE
        loader_layout!!.animate().alpha(1.0f).duration = 1000
        scroll_register_view!!.animate().alpha(0.0f).duration = 250
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this,OnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val userid = firebaseUser?.uid
                    reference = FirebaseDatabase.getInstance().getReference("Users/"+firebaseUser?.uid)
                    val hashMap = HashMap<String, String>()
                    hashMap["id"] = userid.toString()
                    hashMap["nom"] = txt_lastname
                    hashMap["prenom"] = txt_firstname
                    hashMap["username"] = username
                    hashMap["email"] = email
                    hashMap["dateDeNaissance"] = txt_birthdate
                    hashMap["numeroVoie"] = txt_nbVoie
                    hashMap["typeVoie"] = txt_typeVoie
                    hashMap["nomVoie"] = txt_nomVoie
                    hashMap["codePostal"] = txt_codePostal
                    hashMap["ville"] = ville
                    hashMap["telephone"] = telephone
                    hashMap["availableDates"] = ""
                    hashMap["imageURL"] = "default"
                    hashMap["status"] = "offline"
                    hashMap["search"] = username.toLowerCase()
                    reference.setValue(hashMap)
                    progressBar1.visibility = View.GONE
                        Toast.makeText(
                            this@RegisterActivity,
                            getString(R.string.ACTIVITY_REGISTER_SIGN_UP_SUCCESS_FR),
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
            })
    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
    override fun onNothingSelected(parent: AdapterView<*>?) {}
}