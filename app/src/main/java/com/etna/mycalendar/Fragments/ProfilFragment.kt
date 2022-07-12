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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfilFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfilFragment(currentUserModel: UserModel) : Fragment() {
    /** Déclaration de variables  */
    private var editPhoneNumberProfilImageView: ImageView? = null
    /** Déclaration de variables  */
    private  var editAdressProfilImageView:android.widget.ImageView? = null
    /** Déclaration de variables  */
    private  var editCountryProfilImageView:android.widget.ImageView? = null
    /** Déclaration de variables  */
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
    fun ProfilFragment(currentUserModel: UserModel?) {
        this.currentUserModel = currentUserModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profil, container, false)
        /** Initialisation des variables  */
        storageReference = FirebaseStorage.getInstance().getReference("uploads")
        fuser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser?.uid.toString())


        Log.v(TAG, "les info user=" +reference)

        /** On récupère les informations de l'utilisateur  */
        reference?.addListenerForSingleValueEvent(object :ValueEventListener{
            @SuppressLint("SetTextI18n")

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                    var userModel = dataSnapshot.getValue(UserModel::class.java)
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

        // **** SUPRESSION DU COMPTE DES UTILISATEURS ****
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
                                fuser!!.delete() // On supprime le compte de l'utilisateur
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // On suprimme les datas de l'utilisateur dans la RealTime Database
                                            FirebaseDatabase.getInstance().getReference("Users")
                                                .child(
                                                    fuser!!.uid
                                                ).removeValue()
                                            // On affiche un toast de confirmation de suppression du compte
                                            Toast.makeText(
                                                context,
                                                getString(R.string.GENERIC_DELETE_ACCOUNT_TOAST_REDIRECTION_MSG_FR),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Redirection vers la page de login
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

    /** Permet d'ouvrir la gallerie de l'utilisateur  */
    private fun openImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    /**
     * Permet de récupérer l'extension du fichier
     * @param uri
     */
    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = context!!.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    /** Fonction permettant d'uploader la nouvelle image de l'utilisateur sur Firebase Storage  */
   /**private fun uploadImage() {
        val pd = ProgressDialog(context)
        pd.setMessage("Uploading")
        pd.show()
        if (imageUri != null) {
            val fileReference = storageReference!!.child(
                System.currentTimeMillis()
                    .toString() + "." + getFileExtension(imageUri!!)
            )
            uploadTask = fileReference.putFile(imageUri!!)
            (uploadTask as UploadTask).continueWithTask(Continuation<UploadTask.TaskSnapshot?, Task<Uri?>?> { task ->
                if (!task.isSuccessful) {
                    throw (task.exception)!!
                }
                fileReference.downloadUrl
            }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val mUri = downloadUri.toString()
                    reference =
                        FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid)
                    val map = HashMap<String, Any>()
                    map["imageURL"] = mUri
                    reference!!.updateChildren(map)
                    pd.dismiss()
                } else {
                    Toast.makeText(context, "Upload echoué !", Toast.LENGTH_SHORT).show()
                    pd.dismiss()
                }
            }).addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                pd.dismiss()
            })
        } else {
            Toast.makeText(context, "Aucune image n'a été séléctionné", Toast.LENGTH_SHORT).show()
        }
    }**/

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

