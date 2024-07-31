package com.example.bookworm.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookworm.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginRedirect: TextView

    //Declare firebase auth
    private lateinit var mAuth: FirebaseAuth


    //Initialise viewBinding
    private lateinit var binding : ActivityRegisterBinding

    //Declare progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        mAuth = Firebase.auth

        //Initialise progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!!")
        progressDialog.setCanceledOnTouchOutside(false)

        //Handle loginRedirect textview
        binding.txtLoginRedirect.setOnClickListener {
            val myIntent = Intent(this, LoginActivity::class.java)
            startActivity(myIntent)
            finish()
        }

        //Handle register button click
        binding.buttonRegister.setOnClickListener {

            validateData()
        }
    }


    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {

        // Input data
        name = binding.txtRegisterName.text.toString().trim()
        email = binding.txtRegisterEmailAddress.text.toString().trim()
        password = binding.txtRegisterPassword.text.toString().trim()
        val cPassword = binding.txtConfirmPassword.text.toString().trim()

        //Validate data
        if(name.isEmpty()){
            Toast.makeText(this,"Name cannot be empty", Toast.LENGTH_LONG).show()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Toast.makeText(this,"Invalid Email", Toast.LENGTH_LONG).show()
        }else if(password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_LONG).show()
        }else if(cPassword.isEmpty()){
            Toast.makeText(this,"Confirm password cannot be empty", Toast.LENGTH_LONG).show()
        }else if(password != cPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
        }
        else{
            createUserAccount()
        }

    }

    private fun createUserAccount() {
        //Create user account using firebase auth

        //Show progress
        progressDialog.setMessage("Creating account...")
        progressDialog.show()


        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener{
                //Account created ,add user info to db
                updateUserInfo()

            }.addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account due to ${e.message}", Toast.LENGTH_LONG).show()


            }
    }

    private fun updateUserInfo() {
        //Save user information in Firebase db

        //Show progress
        progressDialog.setMessage("Saving user info...")
        //Timestamp
        val timestamp = System.currentTimeMillis()

        //Get current user uid
        val uid = mAuth.uid

        //Setup data to add in the db
        val hashMap:HashMap<String ,Any?> =HashMap()
        hashMap["uid"] = uid
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["profileImg"] = "" //Add empty
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        //Set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener(){
                //Succeeded in adding data to db,open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "User data added to db!!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, UserDashboardActivity::class.java)
                startActivity(intent)
                finish()

            }
            .addOnFailureListener(){
                    e->
                //Failed adding data to db
                progressDialog.dismiss()
                Toast.makeText(this, "Failed adding user data to db due to ${e.message}!!", Toast.LENGTH_LONG).show()
            }
    }

}