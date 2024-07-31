package com.example.bookworm.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookworm.MyApp
import com.example.bookworm.activities.PdfDetailsActivity
import com.example.bookworm.databinding.RowFavouriteBooksBinding
import com.example.bookworm.filters.FilterPdfUser
import com.example.bookworm.models.ModelPdf
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterBookFavourites : RecyclerView.Adapter<AdapterBookFavourites.HolderPdfFavourite>{

    private lateinit var binding : RowFavouriteBooksBinding

    private val context : Context
    public var booksArrayList :ArrayList<ModelPdf>
    private val filterList : ArrayList<ModelPdf>

    //Filter object
    var filter : FilterPdfUser? = null

    constructor(context: Context, booksArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.booksArrayList = booksArrayList
        this.filterList = booksArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavourite {
        binding = RowFavouriteBooksBinding.inflate(LayoutInflater.from(context) ,parent ,false)
        return HolderPdfFavourite(binding.root)
    }

    override fun getItemCount(): Int {
        return  booksArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfFavourite, position: Int) {
        //Get data
        val model = booksArrayList[position]

        loadBookDetails(model,holder)

        //Handle click open pdf details class
        holder.itemView.setOnClickListener {
            val intent = Intent(context,PdfDetailsActivity::class.java)
            intent.putExtra("bookId" ,model.id)
            context.startActivity(intent)
        }
        //Handle click remove from favourites
        binding.removeFavButton.setOnClickListener {
            MyApp.removeFromFavourites(context,model.id)
        }
    }

    private fun loadBookDetails(model: ModelPdf, holder: AdapterBookFavourites.HolderPdfFavourite) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryId = "${snapshot.child("categoryId").value}"
                val description = "${snapshot.child("description").value}"
                val url = "${snapshot.child("url").value}"
                val uid = "${snapshot.child("uid").value}"
                val title = "${snapshot.child("title").value}"
                val viewsCount = "${snapshot.child("viewsCount").value}"
                val downloadsCount = "${snapshot.child("downloadsCount").value}"
                val timestamp = "${snapshot.child("timestamp").value}"

                //Set data to model
                model.isFavourite = true
                model.title=title
                model.description=description
                model.categoryId = categoryId
                model.uid = uid
                model.url = url
                model.downloadsCount = downloadsCount.toLong()
                model.viewsCount = viewsCount.toLong()
                model.timestamp = timestamp.toLong()

                //Format date
                val date = MyApp.formatTimestamp(timestamp.toLong())
                MyApp.loadCategory("$categoryId",holder.categoryTv)
                MyApp.loadPdfFromSinglePage("$url" ,"$title",holder.pdfView,holder.progressBar,null)
                MyApp.loadPdfSize("$url" ,"$title" ,holder.sizeTv)

                holder.titleTv.text = title
                holder.descriptionTv.text = description
                holder.dateTv.text = date


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    inner class HolderPdfFavourite(itemView: View) : RecyclerView.ViewHolder(itemView){

        //Init ui views
        var pdfView: PDFView = binding.pdfView
        var progressBar: ProgressBar = binding.progressBar
        var titleTv: TextView = binding.bookTitleTv
        var descriptionTv: TextView = binding.bookDescriptionTv
        var sizeTv: TextView = binding.bookSizeTv
        var dateTv: TextView = binding.dateTv
        var categoryTv: TextView = binding.bookCategoryTv

    }
}