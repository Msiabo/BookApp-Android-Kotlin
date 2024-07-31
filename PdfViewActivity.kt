package com.example.bookworm.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bookworm.Constants
import com.example.bookworm.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPdfViewBinding

    //Declare bookId
    var bookId = ""

    private companion object{
        private const val TAG = "PDF_VIEW_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Handle click back
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        //Handle back button click
        binding.backButton.setOnClickListener{
            val intent = Intent(this, PdfDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG,"loadBookDetails : Get pdf url from db")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //Get back url
                val pdfUrl = snapshot.child("url").value
                Log.d(TAG,"onDataChange : PDF_URL : $pdfUrl")

                //Load book using url
                loadBookFromUrl("$pdfUrl")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadBookFromUrl(pdfUrl : String) {
        Log.d(TAG,"loadBookFromUrl : Get pdf from firebase storage using url")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG,"loadBookFromUrl : Pdf acquired from url")

                //Load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange{page,pageCount->
                        //Set current and total pages
                        val currentPage = page+1

                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG,"loadBookFromUrl : $currentPage/$pageCount")
                    }
                    .onError {t->
                        Log.d(TAG,"loadBookFromUrl : ${t.message}")
                    }
                    .onPageError{page,t->
                        Log.d(TAG,"loadBookFromUrl : ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {e->
                Log.d(TAG,"loadBookFromUrl : Failed to acquire pdf from url due to ${e.message}")
            }

    }
}