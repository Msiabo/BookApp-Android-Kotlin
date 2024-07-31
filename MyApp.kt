package com.example.bookworm

import android.app.Application
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context

import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.bookworm.activities.PdfDetailsActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import java.util.*

import com.google.firebase.storage.StorageMetadata
import kotlin.collections.HashMap

class MyApp : Application() {
    private lateinit var storage : FirebaseStorage

    //Declare firebase auth
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()

        storage = FirebaseStorage.getInstance()
        mAuth= FirebaseAuth.getInstance()
    }
    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        //Get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            //Get file and metadata using url
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetadata ->
                    Log.d(TAG, "LoadPdfSize : got metadata")
                    val bytes = storageMetadata.sizeBytes.toDouble()
                    Log.d(TAG, "LoadPdfSize : size bytes $bytes")

                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb > 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >= 1) {
                        sizeTv.text = "${String.format("%.2f", kb)}KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)}bytes"
                    }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "LoadPdfSize : Failed to get metadata due to e${e.message}")
                }

        }

        fun loadPdfFromSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_THUMBNAIL_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    Log.d(TAG, "LoadPdfSize : size bytes $bytes")


                    //Set to pdfview
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "LoadPdfFromPdfSinglePage: ${t.message}")
                        }.onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "LoadPdfFromPdfSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG, "loadPdfFromSinglePage : Pages : $nbPages")

                            progressBar.visibility = View.INVISIBLE

                            if (pagesTv != null) {
                                pagesTv.text = "$nbPages"

                            }
                        }
                        .load()


                }.addOnFailureListener { e ->
                    Log.d(TAG, "LoadPdfSize : failed to get metadata due to ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {

            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //Get category
                        val category: String = "${snapshot.child("category").value}"
                        //Set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String) {

            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook : deleting book...")

            //Progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait...")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook : deleting book from storage...")

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook :  book deleted from storage...")
                    Log.d(TAG, "deleteBook : deleting from db now...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {

                            Log.d(TAG, "deleteBook : Successfully deleted from db")
                            Toast.makeText(
                                context,
                                "Successfully deleted from db",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "deleteBook : Failed to delete from db due to ${e.message}")
                            Toast.makeText(
                                context,
                                "Failed to delete from db due to ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                }
                .addOnFailureListener { e ->

                    Log.d(TAG, "deleteBook : Failed to delete from storage due to ${e.message}")
                    Toast.makeText(
                        context,
                        "Failed to delete from storage due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        fun incrementViewsCount(bookId: String) {
            //Get current book views
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Get views count
                    var viewsCount = "${snapshot.child("viewsCount").value}"

                    if (viewsCount == "" || viewsCount == "null") {
                        viewsCount = "0"
                    }
                    //Increment viewsCount
                    val newViewsCount = viewsCount.toLong() + 1

                    //Setup data to change in db
                    val hashMap = HashMap<String, Any>()
                    hashMap["viewsCount"] = newViewsCount

                    //Set to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        public fun removeFromFavourites(context: Context, bookId: String) {
            val TAG = "REMOVE_FAV_TAG"

            val mAuth = FirebaseAuth.getInstance()

            Log.d(TAG, "removeFromFavourites : Removing book from favourites")

            //DB reference
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(mAuth.uid!!).child("Favourites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    //Book removed successfully from favourites
                    Log.d(TAG, "removeFromFavourites : Removed book from favourites")
                    Toast.makeText(context, "Book removed from favourites", Toast.LENGTH_SHORT)
                        .show()
                }
                //Failed to remove book from favourites
                .addOnFailureListener { e ->
                    Log.d(
                        TAG,
                        "removeFromFavourites : Failed to remove book from favourites due to ${e.message}"
                    )
                    Toast.makeText(
                        context,
                        "Failed to remove book from favourites due to ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}