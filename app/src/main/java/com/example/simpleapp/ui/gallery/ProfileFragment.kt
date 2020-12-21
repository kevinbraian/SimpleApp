package com.example.simpleapp.ui.gallery

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.simpleapp.AuthActivity
import com.example.simpleapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import com.bumptech.glide.Glide


class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel

    val REQUEST_IMAGE_CAPTURE = 100
    private lateinit var imageUri: Uri
    private val currentUser = getInstance().currentUser
    private val DEFAULT_IMAGE_URL = "https://picsum.photos/200"
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        //val textView: TextView = root.findViewById(R.id.text_gallery)
        //galleryViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        //})
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        val username: String? = getActivity()?.getIntent()?.getExtras()?.getString("et_email")
        val emailTextView: TextView = view.findViewById(R.id.emailTextView)
        val nombreTextView: TextView =view.findViewById<TextView>(R.id.nameTextView)
        val apellidoTextView: TextView =view.findViewById<TextView>(R.id.apellidoTextView)
        val addressTextView: TextView = view.findViewById<TextView>(R.id.addressTextView)
        val btn_logout =view.findViewById<Button>(R.id.btn_logout)
        val btn_save = view.findViewById<Button>(R.id.btn_save)
        val btn_image = view.findViewById<ImageButton>(R.id.btn_image)
        val progressbar_pic = view.findViewById<ProgressBar>(R.id.progressbar_pic)
        emailTextView.text = username
        if (username != null) {
            db.collection("users").document(username).get().addOnSuccessListener {

                nombreTextView.setText(it.get("nombre") as String?)
                apellidoTextView.setText(it.get("apellido") as String?)
                addressTextView.setText(it.get("address") as String?)
            }.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
        currentUser?.let { user ->
            Glide.with(this)
                .load(user.photoUrl)
                .into(btn_image)
        }

        fun takePictureIntent() {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                    pictureIntent ->
                pictureIntent.resolveActivity(activity?.packageManager!!).also{
                    startActivityForResult(pictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

        //Guardado de datos
        // fichero

        //val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        //prefs.putString("username", username)
        //prefs.apply()

        btn_image.setOnClickListener {
            takePictureIntent()
        }

        btn_logout.setOnClickListener {
            //prefs.clear()
            //prefs.apply()
            getInstance().signOut()
            val authIntent = Intent (getActivity(), AuthActivity::class.java)
            this.startActivity(authIntent)
        }

        btn_save.setOnClickListener {
            val photo = when {
                ::imageUri.isInitialized -> imageUri
                currentUser?.photoUrl == null -> Uri.parse(DEFAULT_IMAGE_URL)
                else -> currentUser.photoUrl
            }
            val updates = UserProfileChangeRequest.Builder()
                .setPhotoUri(photo)
                .build()

            progressbar_pic.visibility = View.VISIBLE
            if (username != null) {
                val profdata = hashMapOf(
                    "nombre" to nombreTextView.text.toString(),
                    "apellido" to apellidoTextView.text.toString(),
                    "address" to addressTextView.text.toString()

                )
                Toast.makeText(getActivity(), "Subiendo a la db", Toast.LENGTH_SHORT).show()
                db.collection("users").document(username).set(profdata)

                currentUser?.updateProfile(updates)
            }
            progressbar_pic.visibility = View.INVISIBLE
        }


    }


    fun uploadImageAndSaveUri(imageBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val progressbar_pic = view?.findViewById<ProgressBar>(R.id.progressbar_pic)
        val btn_image = view?.findViewById<ImageButton>(R.id.btn_image)
        val storageRef = FirebaseStorage.getInstance().reference
            .child("pics/${getInstance().currentUser?.uid}")
        imageBitmap.compress(JPEG, 100, baos)
        val image = baos.toByteArray()

        val upload = storageRef.putBytes(image)

        progressbar_pic?.visibility = View.VISIBLE
        upload.addOnCompleteListener{ uploadTask->
            if(uploadTask.isSuccessful){
                progressbar_pic?.visibility = View.INVISIBLE
                storageRef.downloadUrl.addOnCompleteListener{   urlTask->
                    urlTask.result?.let{
                        imageUri = it
                        Toast.makeText(getActivity(), imageUri.toString(), Toast.LENGTH_SHORT).show()
                        btn_image?.setImageBitmap(imageBitmap)
                    }

                }

            }else{
                uploadTask.exception?.let{
                    Toast.makeText(getActivity(), it.message!!, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    override fun onActivityResult( requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            val imageBitmap = data?.extras?.get("data") as Bitmap

            uploadImageAndSaveUri(imageBitmap)
        }
    }

}