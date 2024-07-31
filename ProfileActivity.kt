package com.example.bookworm.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.bookworm.MyApp
import com.example.bookworm.R
import com.example.bookworm.adapters.AdapterBookFavourites
import com.example.bookworm.databinding.ActivityProfileBinding
import com.example.bookworm.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    //Declare viewBinding
    private lateinit var binding : ActivityProfileBinding

    //Declare firebase auth
    private lateinit var mAuth : FirebaseAuth

    //Declare progress dialog
    private lateinit var progressDialog: ProgressDialog

    //Declare firebase db
    private lateinit var firebaseDb : FirebaseDatabase

    //Declare arrayList
    private lateinit var booksArrayList : ArrayList<ModelPdf>

    private lateinit var adapterPdfFavourite : AdapterBookFavourites

    private lateinit var firebaseUser: FirebaseUser



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Reset ti default views
        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favouriteBooksCountTv.text = "N/A"
        binding.accountStatusTv.text = "N/A"

        //Initialise mAuth
        mAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadFavouriteBooks()



        //Initialise firebaseUser
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        //Initialise progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!!")
        progressDialog.setCanceledOnTouchOutside(false)

        //Handle back button click
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        //Handle button edit profile click
        binding.editProfileButton.setOnClickListener {
            val intent = Intent(this,EditProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Handle click verify if not
        binding.accountStatusTv.setOnClickListener {
            if (firebaseUser.isEmailVerified){

                Toast.makeText(this,"User already verified..", Toast.LENGTH_SHORT).show()
            }else{
                emailVerificationDialog()
            }
        }
    }

    private fun emailVerificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify email")
        builder.setMessage("Are you sure you want to send email verification instructions to your email ${firebaseUser.email}")
            .setPositiveButton("SEND"){d,e->

                sendEmailVerification()
            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }
    }

    private fun sendEmailVerification() {
        //Show progress
        progressDialog.setMessage("Sending email verification instructions to ${firebaseUser.email}")
        progressDialog.show()

        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Instructions sent,check your email ${firebaseUser.email}", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to send instructions due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        if(firebaseUser.isEmailVerified){
            binding.accountStatusTv.text = "Verified"
        }
        else{
            binding.accountStatusTv.text = "Not Verified"
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!)
            .addValueEventListener(object :  ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImg = "${snapshot.child("profileImg").value}"
                    val userType = "${snapshot.child("userType").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"

                    val formattedDate = MyApp.formatTimestamp(timestamp.toLong())

                    //set data
                    binding.fullNameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType

                    //Set image
                    try{
                        Glide.with(this@ProfileActivity)
                            .load(profileImg)
                            .placeholder(R.drawable.person_24)
                            .into(binding.profileImage)

                    }
                    catch(e:Exception){

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private  fun loadFavouriteBooks(){
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favourites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()

                    for (ds in snapshot.children){
                        val bookId = "${ds.child("bookId").value}"

                        //Set to model
                        val modelPdf = ModelPdf()

                        //Add model to list
                        booksArrayList.add(modelPdf)

                    }
                    binding.favouriteBooksCountTv.text = "${booksArrayList.size}"
                    adapterPdfFavourite = AdapterBookFavourites(this@ProfileActivity, booksArrayList)
                    binding.favouriteBooksRv.adapter = adapterPdfFavourite
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}