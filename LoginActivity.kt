package com.example.bookworm.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookworm.R
import com.example.bookworm.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    //Declare progress dialog
    private lateinit var progressDialog: ProgressDialog

    //Declare firebase auth
    private lateinit var mAuth: FirebaseAuth

    private lateinit var registerRedirect : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerRedirect = findViewById(R.id.txtRegisterRedirect);

        // Initialize Firebase Auth
        mAuth = Firebase.auth

        //Initialise progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!!")
        progressDialog.setCanceledOnTouchOutside(false)

        //Handle register redirect textview click
        registerRedirect.setOnClickListener{
            val myIntent = Intent(this, RegisterActivity::class.java)
            startActivity(myIntent)
            finish()
        }
        //Handle login button click
        binding.buttonLogin.setOnClickListener {

            validateData()
        }
        //Handle button forgot password click
        binding.forgotPasswordText.setOnClickListener {
            val intent = Intent(this,ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private var email = ""
    private var password = ""

    private fun validateData() {
        //Input data
        email = binding.txtLoginEmailAddress.text.toString().trim()
        password = binding.txtLoginPassword.text.toString().trim()

        //Validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Please enter a valid email address", Toast.LENGTH_SHORT).show()
        }else if(password.isEmpty()){
            Toast.makeText(this,"Password cannot be empty", Toast.LENGTH_SHORT).show()
        }else{
            loginUser()
        }

    }

    private fun loginUser() {
        //Login user with firebase auth

        //Show progress
        progressDialog.setMessage("Logging in..")
        progressDialog.show()

        mAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener{
                checkUser()

            }.addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account due to ${e.message}", Toast.LENGTH_LONG).show()

            }
    }

    private fun checkUser() {
        /*Check user type - firebase auth
                *If user ,move to user dashboard
                *If admin ,move to admin dashboard*/

        //Show progress
        progressDialog.setMessage("Checking user")

        val firebaseUser = mAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    //Get userType ,user/admin
                    val userType = snapshot.child("userType").value
                    if (userType == "user"){
                        startActivity(Intent(this@LoginActivity, UserDashboardActivity::class.java))
                        finish()
                    }else if (userType == "admin"){
                        startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        finish()
                    }

                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

    }
}