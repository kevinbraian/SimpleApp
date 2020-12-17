package com.example.simpleapp.ui.gallery

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore


class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        //val textView: TextView = root.findViewById(R.id.text_gallery)
        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })
        val db = FirebaseFirestore.getInstance()
        val username: String? = getActivity()?.getIntent()?.getExtras()?.getString("et_email")
        val emailTextView: TextView = root.findViewById(R.id.emailTextView)
        val nombreTextView: TextView = root.findViewById<TextView>(R.id.nameTextView)
        val apellidoTextView: TextView = root.findViewById<TextView>(R.id.apellidoTextView)
        val addressTextView: TextView = root.findViewById<TextView>(R.id.addressTextView)
        val btn_logout = root.findViewById<Button>(R.id.btn_logout)
        val btn_save = root.findViewById<Button>(R.id.btn_save)
        val btn_get = root.findViewById<Button>(R.id.btn_get)
        emailTextView.text = username
        //Guardado de datos
        // fichero

        //val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        //prefs.putString("username", username)
        //prefs.apply()

        btn_logout.setOnClickListener {
            //prefs.clear()
            //prefs.apply()
            FirebaseAuth.getInstance().signOut()
            val authIntent = Intent (getActivity(), AuthActivity::class.java)
            this.startActivity(authIntent)
        }

        btn_save.setOnClickListener {
            if (username != null) {
                val profdata = hashMapOf(
                        "nombre" to nombreTextView.text.toString(),
                        "apellido" to apellidoTextView.text.toString(),
                        "address" to addressTextView.text.toString()
                )
                Toast.makeText(getActivity(), "Subiendo a la db", Toast.LENGTH_SHORT).show()
                db.collection("users").document(username).set(profdata)
            }
        }


        btn_get.setOnClickListener {
            if (username != null) {
                db.collection("users").document(username).get().addOnSuccessListener {

                    nombreTextView.setText(it.get("nombre") as String?)
                    apellidoTextView.setText(it.get("apellido") as String?)
                    addressTextView.setText(it.get("address") as String?)
                }.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
        }


        return root
    }


}