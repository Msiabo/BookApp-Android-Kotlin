package com.example.bookworm.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookworm.filters.FilterPdfUser
import com.example.bookworm.models.ModelPdf
import com.example.bookworm.MyApp
import com.example.bookworm.activities.PdfDetailsActivity
import com.example.bookworm.databinding.RowPdfUserBinding
import com.github.barteksc.pdfviewer.PDFView

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser> ,Filterable{

    private lateinit var binding : RowPdfUserBinding

    private val context : Context
    public var pdfArrayList :ArrayList<ModelPdf>
    private val filterList : ArrayList<ModelPdf>

    //Filter object
    var filter : FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context) ,parent ,false)
        return HolderPdfUser(binding.root)
    }

    override fun getItemCount(): Int {
        return  pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        //Get data
        val model =pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val bookUrl = model.url
        val description = model.description
        val uid = model.uid
        val timestamp = model.timestamp

        val date = MyApp.formatTimestamp(timestamp)

        //Set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        //Pass null value for page number ,as it is not needed here
        MyApp.loadPdfFromSinglePage(bookUrl, title, holder.pdfView, holder.progressBar, null)

        //CategoryId
        MyApp.loadCategory(categoryId, holder.categoryTv)

        //load pdf size
        MyApp.loadPdfSize(bookUrl, title, holder.sizeTv)

        //Handle click open pdf details
        holder.itemView.setOnClickListener {

            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId" ,bookId)
            context.startActivity(intent)
        }
    }
    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView){

        //Init ui views
        var pdfView: PDFView = binding.pdfView
        var progressBar: ProgressBar = binding.progressBar
        var titleTv: TextView = binding.bookTitleTv
        var descriptionTv: TextView = binding.bookDescriptionTv
        var sizeTv: TextView = binding.bookSizeTv
        var dateTv: TextView = binding.dateTv
        var categoryTv: TextView = binding.bookCategoryTv

    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfUser(filterList ,this)
        }
        return filter as FilterPdfUser

    }

}