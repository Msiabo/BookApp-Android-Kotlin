package com.example.bookworm.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData.Item
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.bookworm.MyApp
import com.example.bookworm.R
import com.example.bookworm.databinding.ActivityEditProfileBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.net.URI

class EditProfileActivity : AppCompatActivity() {
    //Declare viewBinding
    private lateinit var binding : ActivityEditProfileBinding
    //Declare mAuth
    private lateinit var mAuth : FirebaseAuth

    //Declare img uri
    private var imgUri : Uri?=null

    //Declare progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Init mAuth
        mAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        //Handle click back button
        binding.backButton.setOnClickListener {
            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Handle click pick image from gallery
        binding.profileImage.setOnClickListener {
            showImageAttachMenu()

        }

        //Handle click submit
        binding.submitButton.setOnClickListener {

            validateData()
        }

    }
    private var name = ""

    private fun validateData() {
        //Get data
        name = binding.nameEt.text.toString().trim()

        if(name.isEmpty()){
            Toast.makeText(this,"Name cannot be empty",Toast.LENGTH_SHORT).show()

        }else{
            if(imgUri == null){

                updateProfile("")
            }else{
                uploadImage()

            }

        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Please wait!!")
        progressDialog.setCanceledOnTouchOutside(false)

        val filePathAndName = "ProfileImages/" + mAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imgUri!!)
            .addOnSuccessListener {taskSnapshot->
                val uriTask : Task<Uri> = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);
                val uploadedImgUrl = "${uriTask.result}"

                updateProfile(uploadedImgUrl)


            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to upload image due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImgUrl: String) {
        progressDialog.setMessage("Updating Profile!!")
        progressDialog.setCanceledOnTouchOutside(false)

        //Setup info to update
        val hashMap : HashMap<String,Any> = HashMap()
        hashMap["name"] = "${name}"
        if(imgUri != null){
            hashMap["profileImg"] = uploadedImgUrl

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(mAuth.uid!!)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Profile updated successfully!!",Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener {e->
                    progressDialog.dismiss()
                    Toast.makeText(this,"Failed to update profile due to ${e.message}!!",Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    val profileImg = "${snapshot.child("profileImg").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    val formattedDate = MyApp.formatTimestamp(timestamp.toLong())

                    //set data
                    binding.nameEt.setText(name)

                    //Set image
                    try{
                        Glide.with(this@EditProfileActivity)
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
    private fun showImageAttachMenu(){
        //Setup popup menu
        val popupMenu = PopupMenu(this,binding.profileImage)
        popupMenu.menu.add(Menu.NONE,0,0,"Camera")
        popupMenu.menu.add(Menu.NONE,1,1,"Gallery")
        popupMenu.show()

        //Handle popup menu click event
        popupMenu.setOnMenuItemClickListener {item->
            //Get id of clicked item
            val id = item.itemId

            if(id == 0){
                //Camera is clicked
                pickImageFromCamera()

            }else if (id == 1){
                //Gallery is clicked
                pickImageFromGallery()

            }
            true
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image from camera
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "images/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageFromCamera() {
        //Intent to pick image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Description")

        imgUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri)
        cameraActivityResultLauncher.launch(intent)
    }
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {result ->
            //Get image uri
            if(result.resultCode == Activity.RESULT_OK){

                val myData = result.data
                imgUri = myData!!.data

                //Set to imageview
                binding.profileImage.setImageURI(imgUri)
            }
            else{
                Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()

            }

        }
    )
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {result->
            if(result.resultCode == Activity.RESULT_OK){

                val myData = result.data
                imgUri = myData!!.data

                //Set to imageview
                binding.profileImage.setImageURI(imgUri)
            }
            else{
                Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()

            }
        }
    )
}