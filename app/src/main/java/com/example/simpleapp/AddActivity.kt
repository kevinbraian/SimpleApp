package com.example.simpleapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class AddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val username = intent.getStringExtra("et_email")
        val user = username!!.toString()
        val addressTextView = findViewById<TextView>(R.id.addressTextView)
        val nameTextView = findViewById<TextView>(R.id.nameTextView)
        val websitephoneTextView = findViewById<TextView>(R.id.websitephoneTextView)
        val typeTextView = findViewById<TextView>(R.id.typeTextView)
        val timeTextView = findViewById<TextView>(R.id.timeTextView)
        val lat = -31.42427474604407
        val long = -64.18379515373843
        val db = FirebaseFirestore.getInstance()
        btnBack.setOnClickListener{
            showNavHome(user)
        }
        btnSave.setOnClickListener {
            val profData = hashMapOf(
                "direccion" to addressTextView.text.toString(),
                "nombre" to nameTextView.text.toString(),
                "webphone" to websitephoneTextView.text.toString(),
                "type"  to typeTextView.text.toString(),
                "time"  to timeTextView.text.toString(),
                "lat"   to lat,
                "long"  to long,
            )
            Toast.makeText(this@AddActivity, "Subiendo a la db", Toast.LENGTH_SHORT).show()
            db.collection("shops").document(username).set(profData)
            showNavHome(user)
        }
    }

    private fun showNavHome(etEmailName: String) {
        val homeIntent = Intent(this, NavActivity::class.java).apply {
            putExtra("et_email", etEmailName)
        }
        this.startActivity(homeIntent)
    }

}