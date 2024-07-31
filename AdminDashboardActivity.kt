package com.example.bookworm.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bookworm.adapters.AdapterCategory
import com.example.bookworm.databinding.ActivityAdminDashboardBinding
import com.example.bookworm.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminDashboardActivity : AppCompatActivity() {

    //Declare firebaseAuth
    private lateinit var mAuth : FirebaseAuth
    //Declare progress dialog
    private lateinit var progressDialog: ProgressDialog

    //Declare viewBinding
    private lateinit var binding : ActivityAdminDashboardBinding

    //ArrayList to hold categories
    private lateinit var categoryArrayList : ArrayList<ModelCategory>

    //Adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialise firebaseAuth
        mAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //Handle logout button click
        binding.logoutBtn.setOnClickListener {
            mAuth.signOut()
            checkUser()
            val myIntent = Intent(this, LoginActivity::class.java)
            startActivity(myIntent)
            finish()

        }
        //Handle add book pdf button
        binding.addBookButton.setOnClickListener {
            val myIntent = Intent(this, AddBookPdfActivity::class.java)
            startActivity(myIntent)
            finish()
        }
        //Handle add category page redirect button click
        binding.addCategoryRedirectButton.setOnClickListener {
           val intent = Intent(this,AddCategoryActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Setup adapter
        adapterCategory = AdapterCategory(this@AdminDashboardActivity,categoryArrayList)
        //Set adapter to recyclerview
        binding.categoriesRv.adapter = adapterCategory

        //Handle click open profile buuton
        binding.openProfileButton.setOnClickListener {
            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    adapterCategory.filter.filter(s)

                }catch(e:Exception){
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.addCategoryRedirectButton.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
            finish()
        }
    }
    private fun loadCategories() {

        //Init arraylist
        categoryArrayList = ArrayList()

        //Get all categories from firebase db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Clear list before adding data to it
                categoryArrayList.clear()

                for(ds in snapshot.children){
                    val model = ds.getValue(ModelCategory :: class.java)

                    //Add to arraylist
                    categoryArrayList.add(model!!)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "DatabaseError: ${error.message}")
            }

        })

    }

    private fun checkUser() {
        //Get current user
        val firebaseUser = mAuth.currentUser
        if (firebaseUser == null) {

            //Not logged ,user can stay in dashboard even when not logged in
            binding.adminSubtitleTv.text = "Not logged in"

            binding.logoutBtn.visibility = View.GONE
            binding.openProfileButton.visibility = View.GONE

        } else {
            //Logged in,show user email
            val email = firebaseUser.email
            binding.adminSubtitleTv.text = email

            binding.logoutBtn.visibility = View.VISIBLE
            binding.openProfileButton.visibility = View.VISIBLE
        }
    }
}