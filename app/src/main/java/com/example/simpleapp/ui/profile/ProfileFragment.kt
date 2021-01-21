package com.example.simpleapp.ui.profile

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.simpleapp.AuthActivity
import com.example.simpleapp.R
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import com.bumptech.glide.Glide


class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    private val requestImageCapture = 100
    private lateinit var imageUri: Uri
    private val currentUser = getInstance().currentUser
    private val defaultImageUrl = "https://picsum.photos/200"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        //val textView: TextView = root.findViewById(R.id.text_profile)
        //profileViewModel.text.observe(viewLifecycleOwner, Observer {
        //textView.text = it
        //})
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username: String? = activity?.intent?.extras?.getString("et_email")
        val emailTextView = view.findViewById<TextView>(R.id.emailTextView)
        val nombreTextView = view.findViewById<TextView>(R.id.nameTextView)
        val apellidoTextView = view.findViewById<TextView>(R.id.websitephoneTextView)
        val addressTextView = view.findViewById<TextView>(R.id.addressTextView)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnImage = view.findViewById<ImageButton>(R.id.btnImage)
        val progressbarPic = view.findViewById<ProgressBar>(R.id.progressbarPic)
        val db = FirebaseFirestore.getInstance()
        emailTextView.text = username

        //Recupera datos de perfil
        if (username != null) {
            db.collection("users").document(username).get().addOnSuccessListener {
                nombreTextView.text = it.get("nombre") as String?
                apellidoTextView.text = it.get("apellido") as String?
                addressTextView.text = it.get("address") as String?
            }.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
        currentUser?.let { user ->
            Glide.with(this)
                .load(user.photoUrl)
                .into(btnImage)
        }
        //Sacar una foto
        fun takePictureIntent() {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { pictureIntent ->
                pictureIntent.resolveActivity(activity?.packageManager!!).also {
                    startActivityForResult(pictureIntent, requestImageCapture)
                }
            }
        }

        //Guardado de datos
        // fichero

        //val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        //prefs.putString("username", username)
        //prefs.apply()

        btnImage.setOnClickListener {
            takePictureIntent()
        }
        //Cerrar sesion
        btnLogout.setOnClickListener {
            //prefs.clear()
            //prefs.apply()
            getInstance().signOut()
            val authIntent = Intent(activity, AuthActivity::class.java)
            this.startActivity(authIntent)
        }
        //Guardar perfil
        btnSave.setOnClickListener {
            val photo = when {
                ::imageUri.isInitialized -> imageUri
                currentUser?.photoUrl == null -> Uri.parse(defaultImageUrl)
                else -> currentUser.photoUrl
            }
            val updates = UserProfileChangeRequest.Builder()
                .setPhotoUri(photo)
                .build()

            progressbarPic.visibility = View.VISIBLE
            if (username != null) {
                val profData = hashMapOf(
                    "nombre" to nombreTextView.text.toString(),
                    "apellido" to apellidoTextView.text.toString(),
                    "address" to addressTextView.text.toString()

                )
                Toast.makeText(activity, "Subiendo a la db", Toast.LENGTH_SHORT).show()
                db.collection("users").document(username).set(profData)

                currentUser?.updateProfile(updates)
            }
            progressbarPic.visibility = View.INVISIBLE
        }


    }

    //Guarda la imagen obtenida
    private fun uploadImageAndSaveUri(imageBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val progressbarPic = view?.findViewById<ProgressBar>(R.id.progressbarPic)
        val btnImage = view?.findViewById<ImageButton>(R.id.btnImage)
        val storageRef = FirebaseStorage.getInstance().reference
            .child("pics/${getInstance().currentUser?.uid}")
        imageBitmap.compress(JPEG, 100, baos)
        val image = baos.toByteArray()
        val upload = storageRef.putBytes(image)

        progressbarPic?.visibility = View.VISIBLE
        upload.addOnCompleteListener { uploadTask ->
            if (uploadTask.isSuccessful) {
                progressbarPic?.visibility = View.INVISIBLE
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    urlTask.result?.let {
                        imageUri = it
                        Toast.makeText(activity, imageUri.toString(), Toast.LENGTH_SHORT).show()
                        btnImage?.setImageBitmap(imageBitmap)
                    }

                }

            } else {
                uploadTask.exception?.let {
                    Toast.makeText(activity, it.message!!, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == requestImageCapture && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            uploadImageAndSaveUri(imageBitmap)
        }
    }

}