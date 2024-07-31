package com.example.bookworm.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

import com.example.bookworm.databinding.ActivityAddBookPdfBinding
import com.example.bookworm.models.ModelCategory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class AddBookPdfActivity : AppCompatActivity() {

    //Setup viewbinding
    private lateinit var binding : ActivityAddBookPdfBinding

    //Firebase storage
    private lateinit var storage : FirebaseStorage

    //Firebase auth
    private lateinit var mAuth : FirebaseAuth

    //Progress dialog while uploading
    private lateinit var progressDialog: ProgressDialog

    //Uri of picked image
    private var pdfUri : Uri? = null

    //Add tag
    private  val TAG ="PDF_ADD_TAG"

    //Arraylist to hold categories
    private lateinit var categoryArrayList :ArrayList<ModelCategory>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        mAuth = Firebase.auth
        loadItemCategories()

        //Initialise progressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait!!")
        progressDialog.setCanceledOnTouchOutside(false)

        //Handle click show categories
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }
        //Handle upload click, start uploading
        binding.uploadButton.setOnClickListener {
            validateData()
        }
        //Handle back button press

        binding.backButton.setOnClickListener {
            val myIntent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(myIntent)
            finish()
        }
        //Handle click pick pdf intent
        binding.attachFileIb.setOnClickListener {
            pdfPickIntent()
        }

    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        //Validate
        Log.d(TAG ,"validateData : validating data")

        //Get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //Validate data
        if(title.isEmpty()){
            Toast.makeText(this,"Title cannot be empty", Toast.LENGTH_LONG).show()
        }else if(description.isEmpty()){
            Toast.makeText(this,"Description cannot be empty", Toast.LENGTH_LONG).show()
        }else if(category.isEmpty()) {
            Toast.makeText(this, "Pick category", Toast.LENGTH_LONG).show()
        }
        else if(pdfUri == null) {
            Toast.makeText(this, "Pick Image", Toast.LENGTH_LONG).show()
        }
        else{
            //Data validated ,begin upload
            uploadPDFToFbStorage()
        }
    }

    private fun uploadPDFToFbStorage() {
        //Upload image to firebase storage
        Log.d(TAG ,"uploadPDFToStorage : uploading pdf to storage...")

        //Show progress
        progressDialog.setMessage("Uploading pdf...")
        progressDialog.show()

        //Timestamp
        val timestamp = System.currentTimeMillis()

        //Path of image in firebase storage
        val filepathAndName = "Books/$timestamp"

        //Storage references
        val storageReference = FirebaseStorage.getInstance().getReference(filepathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot->
                Log.d(TAG ,"uploadPDFToStorage : uploading pdf to storage...")

                //Get image uri
                val uriTask : Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPDFInfoToDb(uploadedPdfUrl ,timestamp)


            }
            .addOnFailureListener{e->
                Toast.makeText(this, "Failed uploading image due to ${e.message}", Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }

    }

    private fun uploadPDFInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfToDb : uploading image info to database...")
        progressDialog.setMessage("Uploading pdf info to db...")

        //Uid of current user
        val uid = mAuth.uid

        //Setup data to upload
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = "$uploadedPdfUrl"
    }

    private fun loadItemCategories() {
        Log.d(TAG ,"loadBookCategories : Loading book categories")

        //Init arraylist
        categoryArrayList = ArrayList()

        //Db reference to load categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Clear list before adding data
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    //Get data
                    val model = ds.getValue(ModelCategory :: class.java)

                    //Add to arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG ,"onDataChange : ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private var selectedCategoryTitle = ""
    private var selectedCategoryId = ""


    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog : Showing item category pick dialog")

        //Get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }
        //Alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                //Handle item click
                //Get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                //Set category to textview
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog :Selected Category ID : $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog : Selected Category Title : $selectedCategoryTitle")
            }.show()
    }
    private fun pdfPickIntent(){
        Log.d(TAG, " pdfPickIntent: Starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->

            if(result.resultCode == RESULT_OK){
                Log.d(TAG ,"PDF picked")
                pdfUri = result.data!!.data
            }else{
                Log.d(TAG ,"PDF pick cancelled")
                Toast.makeText(this,"Cancelled..",Toast.LENGTH_LONG).show()
            }
        }
    )
}