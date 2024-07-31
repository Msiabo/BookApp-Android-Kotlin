package com.example.bookworm.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookworm.databinding.ActivityAddCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddCategoryActivity : AppCompatActivity() {

    //Declare firebaseAuth
    private lateinit var mAuth : FirebaseAuth
    //Declare progress dialog
    private lateinit var progressDialog: ProgressDialog

    //Declare viewBinding
    private lateinit var binding : ActivityAddCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialise firebaseauth
        mAuth = FirebaseAuth.getInstance()

        //Initialise progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!!")
        progressDialog.setCanceledOnTouchOutside(false)

        //Handle back button click
        binding.backButton.setOnClickListener {
            val myIntent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(myIntent)
            finish()

        }


        ////handle click submit ,upload data
        binding.submitButton.setOnClickListener {
            //Call validate data method
            validateData()

        }
    }
    private var category = ""

    private fun validateData() {

        //Get the data
        category = binding.categoryEt.text.toString().trim()

        //Validate the data
        if(category.isEmpty()){
            Toast.makeText(this,"Please enter category name", Toast.LENGTH_SHORT).show()
        }

        else{
            addCategoryFirebase()
        }
    }
    private fun addCategoryFirebase(){
        //Show progress
        progressDialog.show()

        //Get timestamp
        val timestamp = System.currentTimeMillis()

        //Setup data to add in firebase db
        val hashMap=HashMap<String ,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["uid"] = "${mAuth.uid}"
        hashMap["timestamp"] = timestamp

        //Add to firebase db : Database root > Categories > categoryId >categoryInfo
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                //Succeeded in adding data to db
                progressDialog.dismiss()
                Toast.makeText(this, "User data added to db!!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener{e->
                //Failed adding data to db
                progressDialog.dismiss()
                Toast.makeText(this, "Failed adding category to db due to ${e.message}!!", Toast.LENGTH_LONG).show()

            }

    }

}