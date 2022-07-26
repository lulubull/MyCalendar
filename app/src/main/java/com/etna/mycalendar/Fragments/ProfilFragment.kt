package com.etna.mycalendar.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import com.etna.mycalendar.R
import androidx.appcompat.app.AlertDialog
import com.etna.mycalendar.Activity.LoginActivity
import com.etna.mycalendar.Activity.SendEmailActivity
import com.etna.mycalendar.Models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_profil.*
import kotlinx.android.synthetic.main.fragment_profil.view.*

class ProfilFragment(currentUserModel: UserModel) : Fragment() {
    private var editPhoneNumberProfilImageView: ImageView? = null
    private  var editAdressProfilImageView:android.widget.ImageView? = null
    private  var editCountryProfilImageView:android.widget.ImageView? = null
    private  var editZipCodeProfilImageView:android.widget.ImageView? = null
    private lateinit var dataSnapshot :DataSnapshot
    private var reference: DatabaseReference? = null
    private var fuser: FirebaseUser? = null
    private var storageReference: StorageReference? = null
    private val IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var uploadTask: StorageTask<*>? = null
    private var currentUserModel: UserModel? = null
    private var image_profile: CircleImageView? = null

    /** Constructeur  */
    private fun ProfilFragment(currentUserModel: UserModel?) {
        this.currentUserModel = currentUserModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profil, container, false)
        storageReference = FirebaseStorage.getInstance().getReference("uploads")
        fuser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser?.uid.toString())
        Log.v(TAG, "les info user=" +reference)

        reference?.addListenerForSingleValueEvent(object :ValueEventListener{
            @SuppressLint("SetTextI18n")

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userModel = dataSnapshot.getValue(UserModel::class.java)
                    Log.v(TAG, "les info user=" + userModel)
                    view.usernameTextView.setText(userModel?.prenom.toString() + " " + userModel?.nom)
                    view.pseudoTextView.setText( userModel?.username.toString()  )
                    view.emailUserTextView.setText(  userModel?.email.toString() )
                    view.telephoneTextView.setText( userModel?.telephone.toString() )
                    view.adresseTextView.setText(userModel?.numeroVoie.toString() + " " + userModel?.typeVoie + " " + userModel?.nomVoie
                    )
                    villeTextView.setText( userModel?.ville.toString() )
                    codePostalTextView.setText(userModel?.codePostal.toString() )
                    if (userModel?.imageURL?.equals("default") == true) {
                        image_profile?.setImageResource(R.mipmap.ic_launcher)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        image_profile?.setOnClickListener(View.OnClickListener { openImage() })
        view.deconnectionButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(
                    activity,
                    LoginActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
        view.sendMail.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, SendEmailActivity::class.java)
            startActivity(intent)
        })
        view.editPhoneNumberProfilImageView.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            bundle.putString("editPhoneNumber", "editPhoneNumber")
            val editProfilDialog = EditProfilDialog()
            editProfilDialog.setArguments(bundle)
            fragmentManager?.let { it1 -> editProfilDialog.show(it1, "EditProfilDialog") }
        })
        view.editAdressProfilImageView.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            bundle.putString("editAddress", "editAddress")
            val editProfilDialog = EditProfilDialog()
            editProfilDialog.setArguments(bundle)
            fragmentManager?.let { it1 -> editProfilDialog.show(it1, "EditProfilDialog") }
        })
        view.editCountryProfilImageView.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            bundle.putString("editCountry", "editCountry")
            val editProfilDialog = EditProfilDialog()
            editProfilDialog.setArguments(bundle)
            fragmentManager?.let { it1 -> editProfilDialog.show(it1, "EditProfilDialog") }
        })
        view.editZipCodeProfilImageView.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            bundle.putString("editZip", "editZip")
            val editProfilDialog = EditProfilDialog()
            editProfilDialog.setArguments(bundle)
            fragmentManager?.let { it1 -> editProfilDialog.show(it1, "EditProfilDialog") }
        })

        // delete acount
        view.deleteAccountButton?.setOnClickListener(View.OnClickListener {
            AlertDialog.Builder((context)!!)
                .setTitle(getString(R.string.GENERIC_DELETE_ACCOUNT_FR))
                .setMessage(getString(R.string.GENERIC_YOU_WANT_TO_DELETE_YOUR_ACCOUNT_ARE_YOU_SURE_FR))
                .setPositiveButton(
                    getString(R.string.GENERIC_YES_FR)
                ) { dialogInterface, i ->
                    AlertDialog.Builder((context)!!)
                        .setTitle(getString(R.string.GENERIC_DELETE_ACCOUNT_FR))
                        .setMessage(getString(R.string.GENERIC_YOU_WANT_TO_DELETE_YOUR_ACCOUNT_ARE_YOU_SURE_CONFIRM_FR))
                        .setPositiveButton(
                            getString(R.string.GENERIC_YES_DELETE_ACCOUNT_FR)
                        ) { dialogInterface, i ->
                            if (fuser != null) {
                                fuser!!.delete()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            FirebaseDatabase.getInstance().getReference("Users")
                                                .child(
                                                    fuser!!.uid
                                                ).removeValue()
                                            Toast.makeText(
                                                context,
                                                getString(R.string.GENERIC_DELETE_ACCOUNT_TOAST_REDIRECTION_MSG_FR),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            startActivity(
                                                Intent(
                                                    activity,
                                                    LoginActivity::class.java
                                                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            )
                                        }
                                    }
                            }
                        }
                        .setNegativeButton(
                            getString(R.string.GENERIC_CANCEL_FR)
                        ) { dialog, which -> dialog.dismiss() }
                        .show()
                }
                .setNegativeButton(
                    getString(R.string.GENERIC_CANCEL_FR)
                ) { dialog, which -> dialog.dismiss() }
                .show()
        })
        return view
    }

    /** open lst users  */
    private fun openImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = context!!.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == IMAGE_REQUEST) && (resultCode == Activity.RESULT_OK
                    ) && (data != null) && (data.data != null)
        ) {
            imageUri = data.data
            if (uploadTask != null && uploadTask!!.isInProgress) {
                Toast.makeText(context, "Upload en cours", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

