package com.seaweed.attends

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.content.SharedPreferences



class MainActivity : AppCompatActivity() {
    // DECLARE - Instances
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

//        SharedPreferences of LOGIN
        initLogin()

        // get reference to all views
        var et_user_name = findViewById(R.id.et_user_name) as EditText
        var et_password = findViewById(R.id.et_password) as EditText
        var btn_reset = findViewById(R.id.btn_reset) as Button
        var btn_submit = findViewById(R.id.btn_submit) as Button

        btn_reset.setOnClickListener {
            // clearing user_name and password edit text views on reset button click
            et_user_name.setText("")
            et_password.setText("")
        }

        // set on-click listener
        btn_submit.setOnClickListener {
            val user_name = et_user_name?.text;
            val password = et_password?.text;
//            Toast.makeText(this@MainActivity, user_name , Toast.LENGTH_LONG).show()
            signInWithEmail(email = user_name.toString(), password = password.toString())
            // your code to validate the user_name and password combination
            // and verify the same

        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//                    updateUI(currentUser)
    }

    public fun signInWithEmail(email: String?, password: String?){
        auth.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }

    public fun signupBtnClicked(email: String?, password: String?){
        auth.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    createLoginSession(user, password)
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }

    private fun updateUI(user: FirebaseUser?){
        if(user != null){
//            Log.d(TAG, user.uid.toString())
            Toast.makeText(baseContext, user.uid.toString(),
                Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        } else {
            Log.d(TAG, "Login Failed.")
        }
        Toast.makeText(baseContext, "updateUI.",
            Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "updateUI.")
    }

    private  fun createLoginSession(user: FirebaseUser?, password: String?){
        val editor = getSharedPreferences("myPref", Context.MODE_PRIVATE).edit()
        val reader = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        editor.putString("email", user?.email.toString())
        editor.putString("id", (user?.uid).toString())
        editor.putString("password", password)
        editor.commit();
        val email = reader.getString("email", "-1")
        Toast.makeText(baseContext, user?.email+" : ",
            Toast.LENGTH_SHORT).show()
    }

    private fun initLogin(){
        val reader = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        val email = reader.getString("email", "-1")
        val password = reader.getString("password", "-1")
//        Toast.makeText(baseContext, "IniLogin : "+email+" , "+password,
//            Toast.LENGTH_SHORT).show()
        if(email != "-1" || password != "-1"){
            signInWithEmail(email, password)
        }
    }


    companion object {
        private const val TAG = "EmailPassword"
    }
}
