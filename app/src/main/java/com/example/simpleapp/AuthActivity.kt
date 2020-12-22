package com.example.simpleapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth.*
import com.google.firebase.auth.GoogleAuthProvider
import java.util.regex.Pattern.compile as compile1

class AuthActivity : AppCompatActivity() {
    override fun onStart() {
        val authLayout = findViewById<LinearLayout>(R.id.AuthLayout)
        super.onStart()
        authLayout.visibility = View.VISIBLE
    }
    //Valider usuario y contraseña, deben no ser vacios, tener mayusculas, minusculas, numeros y un simbolo especial
    private fun validation(): Boolean {
        val upperCasePattern = compile1("[A-Z]")
        val lowerCasePattern = compile1("[a-z]")
        val digitCasePattern = compile1("[0-9]")
        val specialCharPattern = compile1("[/*!@#$%^&()¨{}|?<>]")
        val etEmailName = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val emailName = etEmailName.text
        val password = etPassword.text
        val characters = listOf(
            '"',
            '\'',
            ',',
            'ü',
            'Ü',
            'á',
            'é',
            'í',
            'ó',
            'ú',
            'Á',
            'É',
            'Í',
            'Ó',
            'Ú',
            '\n',
            '\r'
        )
        val flag = true
        //vacio?
        if (emailName.isEmpty() or password.isEmpty()) {
            Toast.makeText(
                this@AuthActivity,
                "el usuario y/o contraseña no debe estar vacio",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        //mayor a 8 caracteres?
        if ((emailName.length < 8) or (password.length < 8)) {
            Toast.makeText(
                this@AuthActivity,
                "El usuario y/o contraseña debe tener al menos 8 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        for (x in characters.indices) {
            if ((emailName.indexOf(characters[x]) != -1) or (password.indexOf(characters[x]) != -1)) {
                Toast.makeText(
                    this@AuthActivity,
                    "El usuario no puede tener los siguientes caracteres : Comillas, punto y coma, diéresis, tildes, escapes",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        //Mayusculas
        if (!upperCasePattern.matcher(password).find()) {
            Toast.makeText(
                this@AuthActivity,
                "La contraseña debe tener una mayúscula",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        //Minusculas
        if (!lowerCasePattern.matcher(password).find()) {
            Toast.makeText(
                this@AuthActivity,
                "La contraseña debe tener una minúscula",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        //Numeros
        if (!digitCasePattern.matcher(password).find()) {
            Toast.makeText(
                this@AuthActivity,
                "La contraseña debe tener un número",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        //Simbolo especial
        if (!specialCharPattern.matcher(password).find()) {
            Toast.makeText(
                this@AuthActivity,
                "La contraseña debe tener un caracter especial",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return flag
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        //Google Analytics
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        analytics.logEvent("InitScreen", bundle)

        title = "Bienvenido"

        val etEmailName = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSubmit = findViewById<Button>(R.id.btnLogout)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoogle = findViewById<Button>(R.id.googleButton)
        val googleSignIn = 1
        //Iniciar con Google
        btnGoogle.setOnClickListener {
            //Configuracion
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, googleSignIn)
            session()
        }
        //Iniciar sesion
        btnSubmit.setOnClickListener {
            val emailName = etEmailName.text
            val user = emailName.toString()
            val password = etPassword.text
            if (validation()) {
                getInstance().signInWithEmailAndPassword(emailName.toString(), password.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@AuthActivity,
                                getString(R.string.logsucc),
                                Toast.LENGTH_SHORT
                            ).show()
                            showNavHome(user)
                        } else {

                            showAlert()
                        }
                    }
            }
            //Se logueara el usuario, si existe lo lleva al Home, si no, lo notifica.
        }
        //Registrarse
        btnRegister.setOnClickListener {
            val emailName = etEmailName.text
            val user = emailName.toString()
            val password = etPassword.text
            if (validation()) {
                //Se creara el usuario
                getInstance().createUserWithEmailAndPassword(user, password.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(
                                this@AuthActivity,
                                "Register Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            showNavHome(user)
                        } else {
                            this.showAlert()
                        }
                    }
            }
        }


    }
    //Pasa a la actividad nav
    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE)
        val user = prefs.getString("etEmailName", null)
        if (user != null) {
            val authLayout = findViewById<LinearLayout>(R.id.AuthLayout)
            authLayout.visibility = View.INVISIBLE
            showNavHome(user)
        }
    }
    //Mensaje de error
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al iniciar sesión, revise el usuario y/o contraseña")
        builder.setPositiveButton("aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val googleSignIn = 1
        if (requestCode == googleSignIn) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val user = findViewById<EditText>(R.id.etEmail).text.toString()
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            showNavHome(user)
                        } else {
                            showAlert()
                        }
                    }
                }
            } catch (e: ApiException) {
                showAlert()
            }
        }

    }


    private fun showNavHome(etEmailName: String) {
        val homeIntent = Intent(this, NavActivity::class.java).apply {
            putExtra("et_email", etEmailName)
        }
        this.startActivity(homeIntent)
    }


}