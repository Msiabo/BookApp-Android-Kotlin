package com.example.bookworm.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.bookworm.adapters.AdapterPdfAdmin
import com.example.bookworm.databinding.ActivityPdfListAdminBinding
import com.example.bookworm.models.ModelPdf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {

    private var categoryId = ""
    private var category = ""

    private lateinit var pdfArrayList :ArrayList<ModelPdf>
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //Declare firebaseAuth
    private lateinit var mAuth : FirebaseAuth
    //Declare progress dialog
    private lateinit var progressDialog: ProgressDialog

    //Declare viewBinding
    private lateinit var binding : ActivityPdfListAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //Set pdf category
        binding.subtitleTv.text = category

        //load books/pdf
        loadPdfList()

        //Handle click back
        binding.backButton.setOnClickListener {

            val intent = Intent(this, AdminDashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{
                    adapterPdfAdmin.filter!!.filter(s)

                }catch(e:Exception){
                    Log.d(TAG ,"onTextChanged : ${e.message} ")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

    }

    private fun loadPdfList() {
        //Initialise arraylist
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Clear list before adding data into it
                    pdfArrayList.clear()

                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelPdf :: class.java)

                        //Add to list
                        if(model != null){
                            pdfArrayList.add(model)
                            Log.d(TAG, "onDataChange : ${model.title} ${model.categoryId}")
                        }

                    }
                    //Setup adapter
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity ,pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}