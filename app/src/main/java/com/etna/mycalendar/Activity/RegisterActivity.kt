package com.etna.mycalendar.Activity

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        val addressTypeSpinner = findViewById<Spinner>(R.id.adressTypeSpinner)
        val addressTypeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.adressTypeSpinner, android.R.layout.simple_spinner_item
        )
        addressTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        addressTypeSpinner.adapter = addressTypeAdapter
        addressTypeSpinner.setOnItemSelectedListener(this)
        btn_register.setOnClickListener(View.OnClickListener {
            val txtUsername: String = username.getText().toString()
            val txtEmail: String = email.getText().toString()
            val txtLastName = lastname.getText().toString()
            val txtFirstName: String = firstname.getText().toString()
            val txtBirthDate: String = birthDate.getText().toString()
            val txtNbVoie: String = numeroVoieET.getText().toString()
            val txtTypeVoie = addressTypeSpinner.getSelectedItem().toString()
            val txtNomVoie: String = adressNameET.getText().toString()
            val txtCodePostal: String = codePostal.getText().toString()
            val txtTownName: String = townName.getText().toString()
            val txtTelephone: String = numeroTelephoneET.getText().toString()
            val txtPassword: String = passwordTxt.getText().toString()
            registrationProcess(
                txtUsername,
                txtEmail,
                txtPassword,
                txtLastName,
                txtFirstName,
                txtBirthDate,
                txtNbVoie,
                txtTypeVoie,
                txtNomVoie,
                txtCodePostal,
                txtTownName,
                txtTelephone
            )
        })
    }

    private fun registrationProcess(
        username: String,
        email: String,
        password: String,
        txtLastName: String,
        txtFirstName: String,
        txtBirthDate: String,
        txtNbVoie: String,
        txtTypeVoie: String,
        txtNomVoie: String,
        txtCodePostal: String,
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
                    hashMap["nom"] = txtLastName
                    hashMap["prenom"] = txtFirstName
                    hashMap["username"] = username
                    hashMap["email"] = email
                    hashMap["dateDeNaissance"] = txtBirthDate
                    hashMap["numeroVoie"] = txtNbVoie
                    hashMap["typeVoie"] = txtTypeVoie
                    hashMap["nomVoie"] = txtNomVoie
                    hashMap["codePostal"] = txtCodePostal
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