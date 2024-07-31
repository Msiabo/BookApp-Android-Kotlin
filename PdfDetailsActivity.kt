package com.example.bookworm.activities

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookworm.Constants
import com.example.bookworm.MyApp
import com.example.bookworm.R
import com.example.bookworm.adapters.AdapterComment
import com.example.bookworm.databinding.ActivityPdfDetailsBinding
import com.example.bookworm.databinding.DialogCommentAddBinding
import com.example.bookworm.databinding.RowCommentBinding
import com.example.bookworm.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream


class PdfDetailsActivity : AppCompatActivity() {
    //Declare viewBinding
    private lateinit var binding : ActivityPdfDetailsBinding

    //Declare progressDialog
    private lateinit var progressDialog: ProgressDialog

    //Book Id
    private var bookId = ""

    private var bookTitle = ""

    private var bookUrl = ""

    private var isMyFavourote = false

    private companion object{
        private const val TAG = "BOOK_DETAILS_TAG"
    }
    //Declare firebase auth
    private lateinit var mAuth: FirebaseAuth

    private lateinit var commentArrayList: ArrayList<ModelComment>

    private lateinit var adapterComment : AdapterComment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        mAuth = Firebase.auth
        if(mAuth.currentUser != null){
            checkIsFavourite()
        }

        //Get bookId from intent
        bookId = intent.getStringExtra("bookId")!!

        //Handle back button click
        binding.backButton.setOnClickListener{
            val intent = Intent(this, PdfListAdminActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Increment viewsCount whenever this page starts
        MyApp.incrementViewsCount(bookId)
        
        loadBookDetails()
        showComments()

        //Handle click add/remove favourite
        binding.AddToFavouritesButton.setOnClickListener {

            if(mAuth.currentUser == null){
                Toast.makeText(this,"You are not logged in",Toast.LENGTH_SHORT).show()
            }
            else{
                if(isMyFavourote){
                    MyApp.removeFromFavourites(this,bookId)
                }else{
                    addToFavourites()
                }

            }
        }
        //Handle click show add comment dialog
        binding.addCommentButton.setOnClickListener {
            if (mAuth.currentUser == null){

                Toast.makeText(this,"You are not logged in",Toast.LENGTH_SHORT).show()
            }else{
                addCommentDialog()
            }
        }


        //Handle click open pdf view
        binding.readBookButton.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId",bookId)
            startActivity(intent)
            finish()
        }
        //Handle click download book
        binding.downloadBookButton.setOnClickListener {
            // Check if the permission is already granted
            // Check if the permission is already granted
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG ,"onCreate : Storage Permission already granted")

            }else{
                Log.d(TAG ,"onCreate : Storage Permission not granted")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

    }

    private fun showComments() {
        commentArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelComment::class.java)

                        commentArrayList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@PdfDetailsActivity,commentArrayList)

                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private var comment = ""

    private fun addCommentDialog() {

        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this ,R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        //Create and show alertdialog
        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.backButton.setOnClickListener {
            alertDialog.dismiss()
        }
        commentAddBinding.submitButton.setOnClickListener {
            //Get data
            comment = commentAddBinding.addCommentEt.text.toString().trim()

            //Validate data
            if (comment.isEmpty()){

                Toast.makeText(this,"Enter comment..",Toast.LENGTH_SHORT).show()
            }else{
                alertDialog.dismiss()
                addComment()

            }
        }
    }

    private fun addComment() {
        progressDialog.setMessage("Adding comment...")
        progressDialog.show()

        val timestamp = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookTitle"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${mAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .child("Comments")
            .child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {

                Toast.makeText(this,"Update Successful" ,Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {e->

                Toast.makeText(this,"Failed to update book due to ${e.message}" ,Toast.LENGTH_SHORT).show()

            }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted : Boolean->
        if(isGranted){
            Log.d(TAG ,"onCreate : Storage Permission already granted")
            downloadBook()

        }
        else{
            Log.d(TAG ,"onCreate : Storage Permission already granted")
            Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
        }

    }

    private fun downloadBook(){
        //ProgressBar
        progressDialog.setMessage("Downloading book...")
        progressDialog.show()

        //Lets download book firebase storage using url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG ,"downloadBook : Book downloaded...")
                saveToDownloadsFolder(bytes)

            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.d(TAG ,"downloadBook : Failed to download book due to ${e.message}")
                Toast.makeText(this,"Failed to download due to ${e.message}" ,Toast.LENGTH_SHORT).show()

            }
    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {

        Log.d(TAG ,"saveToDownloadsFolder : Saving to downloads folder")

        val nameWithExtension = "$bookTitle.pdf"

        try {

            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs()

            val filePath = downloadsFolder.path + "/" + nameWithExtension

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Log.d(TAG ,"saveToDownloadsFolder : Saved to downloads folder")
            Toast.makeText(this,"Saved to downloads folder" ,Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            incrementDownloadsCount()
        }
        catch (e:Exception){
            Log.d(TAG ,"saveToDownloadsFolder : Failed to save to downloads folder due to ${e.message}")
            Toast.makeText(this," Failed to save to downloads folder due to ${e.message}" ,Toast.LENGTH_SHORT).show()

        }
    }

    private fun incrementDownloadsCount() {

        //Increment downloads count to firebase db
        Log.d(TAG,"incrementDownloadsCount : ")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Get downloads count
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG,"onDataChange : Current downloads count $downloadsCount")

                    if(downloadsCount == "" || downloadsCount == "null"){
                        downloadsCount = "0"
                    }
                    //Convert to long to increment
                    val newDownloadsCount : Long= downloadsCount.toLong() + 1
                    Log.d(TAG,"onDataChange : New downloads count $newDownloadsCount")

                    //Setup data to update to db
                    val hashMap : HashMap<String,Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadsCount

                    //Setup new incremented downloads count to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {

                            Log.d(TAG,"onDataChange : Downloads count implemented")
                        }
                        .addOnFailureListener {e->
                            Log.d(TAG,"onDataChange : Failed to implement downloads count due to ${e.message}")

                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadBookDetails() {
        //Books > bookId >Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount ="${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("categoryId").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //Format date
                    val date = MyApp.formatTimestamp(timestamp.toLong())

                    MyApp.loadCategory(categoryId, binding.categoryTv)

                    //Load pdf thumbnail
                    MyApp.loadPdfFromSinglePage(
                        "$bookUrl",
                        "$bookTitle",
                        binding.pdfView,
                        binding.progressBar,
                        binding.pagesTv
                    )
                    //Load pdf size
                    MyApp.loadPdfSize("$bookUrl", "$bookTitle", binding.sizeTv)

                    //Set data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun checkIsFavourite(){

        Log.d(TAG,"checkIsFavourite : Checking whether book is in user's favourites or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("Books").child("Favourites").child(bookId)
            .addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isMyFavourote = snapshot.exists()
                    if(isMyFavourote){
                        //Available in favourites
                        Log.d(TAG,"onDataChange : Available in favourites")
                        binding.AddToFavouritesButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.favorite_24,0,0)
                        binding.AddToFavouritesButton.text = "Remove from favourites"
                    }
                    else{
                        //No available in favourites
                        Log.d(TAG,"onDataChange : Not available in favourites")
                        binding.AddToFavouritesButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.baseline_favorite_border_24,0,0)
                        binding.AddToFavouritesButton.text = "Add to favourites"

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun addToFavourites(){
        Log.d(TAG,"addToFavourites : Adding to favourites...")
        val timestamp = System.currentTimeMillis()

        //Setup data to add to db
        val hashMap = HashMap<String ,Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //Save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(mAuth.uid!!).child("Favourites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //Added to db successfully
                Log.d(TAG,"addToFavourites : Book added to favourites")
                Toast.makeText(this,"Book added to favourites",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                //Failed to add to db
                Log.d(TAG,"addToFavourites : Failed to add book to favourites due to ${e.message}")
                Toast.makeText(this,"Failed to add book to favourites due to ${e.message}",Toast.LENGTH_SHORT).show()

            }

    }

}