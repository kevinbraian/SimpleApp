package com.example.simpleapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.simpleapp.R.id
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class NavActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav)
        val toolbar: Toolbar = findViewById(id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout = findViewById<DrawerLayout>(id.drawer_layout)
        val navView = findViewById<NavigationView>(id.nav_view)
        val headerView = navView.getHeaderView(0)
        val navController = findNavController(id.nav_host_fragment)
        val email = headerView.findViewById<TextView>(id.emailTextView)
        val username = intent.getStringExtra("et_email")
        val profileImage: ImageView = headerView.findViewById(id.avatarView)
        //Carga nombre y foto en el header del menu izquierdo
        email.text = username
        currentUser?.let { user ->
            Glide.with(this)
                .load(user.photoUrl)
                .into(profileImage)
        }
        val fab: FloatingActionButton = findViewById(id.fab)
        fab.setOnClickListener {
            showAdd(username!!)
        }
        //Fragmentos en el menu izquierdo
        appBarConfiguration = AppBarConfiguration(
            setOf(
                id.nav_home, id.nav_profile, id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    private fun showAdd(etEmailName: String) {
        val addIntent = Intent(this, AddActivity::class.java).apply {
            putExtra("et_email", etEmailName)
        }
        this.startActivity(addIntent)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.nav, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}