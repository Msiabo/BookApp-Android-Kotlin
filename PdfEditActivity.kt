package com.example.bookworm.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bookworm.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    //Declare viewBinding
    private lateinit var binding : ActivityPdfEditBinding

    //Declare firebase db
    private lateinit var firebasedb : FirebaseDatabase

    private var bookId = ""
    private lateinit var progressDialog: ProgressDialog

    //ArrayList to hold category titles
    private lateinit var categoryTitleArrayList : ArrayList<String>

    //ArrayList to hold category Ids
    private lateinit var categoryIdArrayList : ArrayList<String>

    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //Get book id to edit book info
        bookId = intent.getStringExtra("bookId")!!

        loadCategories()

        loadBookInfo()

        //Handle click back
        binding.backButton.setOnClickListener{
            val intent = Intent(this, PdfListAdminActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Handle click pick category
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }
        //Handle click update button
        binding.updateButton.setOnClickListener {
            validateData()
        }

    }

    private fun loadBookInfo() {
        Log.d(TAG ,"loadBookInfo : Loading book info...")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    //Set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //Load book category info using category Id
                    Log.d(TAG ,"onDataChange : Loading category info...")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object  : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //Get category
                                val category = snapshot.child("category").value

                                //Set to textview
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private var title = ""
    private var description = ""

    private fun validateData() {
        //Validate
        Log.d(TAG ,"validateData : Validating data")

        //Get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()


        //Validate data
        if(title.isEmpty()){
            Toast.makeText(this,"Enter book title", Toast.LENGTH_LONG).show()
        }else if(description.isEmpty()){
            Toast.makeText(this,"Enter book description", Toast.LENGTH_LONG).show()
        }
        else{
            //Data validated ,begin upload
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf : starting pdf update...")

        //Set progress dialog
        progressDialog.setMessage("Updating book info...")
        progressDialog.show()


        //Setup data to update ,spellings of keys must be the same as firebase
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        //Start updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG ,"updatePdf : Update successful..")
                Toast.makeText(this,"Update Successful" ,Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {e->
                Log.d(TAG ,"updatePdf : Failed to update book due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this,"Failed to update book due to ${e.message}" ,Toast.LENGTH_SHORT).show()

            }
    }

    private var selectedCategoryTitle = ""
    private var selectedCategoryId = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog : Showing item category pick dialog")

        //Get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }
        //Alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, position ->
                //Handle item click
                //Save clicked category and title
                selectedCategoryTitle = categoryTitleArrayList[position]
                selectedCategoryId = categoryIdArrayList[position]

                //Set category to textview
                binding.categoryTv.text = selectedCategoryTitle

            }.show()
    }

    private fun loadCategories() {
        Log.d(TAG ,"loadCategories : loading categories...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //Clear list before adding data to it
                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()

                for(ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "" + ds.child("category").value

                    categoryTitleArrayList.add(category)
                    categoryIdArrayList.add(id)

                    Log.d(TAG ,"onDataChange : Category Id $id")
                    Log.d(TAG ,"onDataChange : Category Title $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}