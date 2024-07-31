package com.example.bookworm

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bookworm.adapters.AdapterPdfUser
import com.example.bookworm.databinding.FragmentBooksUserBinding
import com.example.bookworm.models.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BooksUserFragment : Fragment {

    private lateinit var binding : FragmentBooksUserBinding

    public companion object{
        private const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryId : String ,category: String ,uid:String):BooksUserFragment{
            val fragment = BooksUserFragment()

            //Put data to bundle intent
            val args = Bundle()
            args.putString("categoryId" ,categoryId)
            args.putString("category" ,category)
            args.putString("uid" ,uid)
            fragment.arguments = args
            return fragment
        }
    }
    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList :ArrayList<ModelPdf>
    private lateinit var adapterPdfUser : AdapterPdfUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Get arguments passed from newInstance
        val args = arguments
        if (args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentBooksUserBinding.inflate(LayoutInflater.from(context),container,false)

        //Load book according to category
        Log.d(TAG,"onCreateView : Category $category")
        if (category == "All"){

            loadAllBooks()
        }
        else if(category == "Most Viewed"){
            //Load most viewed books

            loadMostViewedDownloadedBooks("viewsCount")
        }
        else if(category == "Most Downloaded"){
            //Load most downloaded books
            loadMostViewedDownloadedBooks("downloadsCount")

        }
        else{
            loadCategorisedBooks()

        }
        //Search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try{

                    adapterPdfUser.filter?.filter(s)

                }catch(e:Exception){
                    Log.d(TAG,"onTextChanged : SEARCH EXCEPTION ${e.message}")
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return binding.root
    }

    private fun loadAllBooks() {
        //Initialise arraylist
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Clear list before adding data to it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)

                    //Add to list
                    pdfArrayList.add(model!!)
                }
                //Setup adapter
                adapterPdfUser = AdapterPdfUser(context!! ,pdfArrayList)
                //Set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy : String) {
        //Initialise arraylist
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Clear list before adding data to it
                pdfArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelPdf::class.java)

                    //Add to list
                    pdfArrayList.add(model!!)
                }
                //Setup adapter
                adapterPdfUser = AdapterPdfUser(context!! ,pdfArrayList)
                //Set adapter to recyclerview
                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadCategorisedBooks() {

        //Initialise arraylist
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Clear list before adding data to it
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelPdf::class.java)

                        //Add to list
                        pdfArrayList.add(model!!)
                    }
                    //Setup adapter
                    adapterPdfUser = AdapterPdfUser(context!! ,pdfArrayList)
                    //Set adapter to recyclerview
                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}