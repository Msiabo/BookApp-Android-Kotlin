package com.example.bookworm.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookworm.filters.FilterPdfAdmin
import com.example.bookworm.models.ModelPdf
import com.example.bookworm.MyApp
import com.example.bookworm.activities.PdfDetailsActivity
import com.example.bookworm.activities.PdfEditActivity
import com.example.bookworm.databinding.RowPdfAdminBinding
import com.github.barteksc.pdfviewer.PDFView

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>,Filterable{

    private lateinit var binding : RowPdfAdminBinding

    private val context : Context
    public var pdfArrayList :ArrayList<ModelPdf>
    private val filterList : ArrayList<ModelPdf>

    //Filter object
    var filter : FilterPdfAdmin? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {

        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context) ,parent ,false)
        return HolderPdfAdmin(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        //Get data
        val model =pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val pdfUrl = model.url
        val description = model.description
        val uid = model.uid
        val timestamp = model.timestamp
        val formattedDate = MyApp.formatTimestamp(timestamp)

        //Set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //Load further details
        //CategoryId
        MyApp.loadCategory(categoryId, holder.categoryTv)

        //Pass null value for page number ,as it is not needed here
        MyApp.loadPdfFromSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null)

        //load pdf size
        MyApp.loadPdfSize(pdfUrl, title, holder.sizeTv)

        //Handle click show dialog with options 1)Edit book 2)Delete book
        holder.moreButton.setOnClickListener {
            moreOptionsDialog(model ,holder)
        }
        //Handle item click ,open bookDetails
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfDetailsActivity::class.java)
            intent.putExtra("bookId" ,pdfId)
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin) {
        //Get id ,url ,and title of book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //options to show in dialog
        val options = arrayOf("Edit" ,"Delete")

        //Alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose an option")
            .setItems(options){dialog ,position->
                //Handle item click
                if(position == 0){
                    //Edit is clicked
                    val intent = Intent(context , PdfEditActivity::class.java)
                    intent.putExtra("bookId" ,bookId)
                    context.startActivity(intent)

                }else if(position == 1){
                    //Delete is clicked
                    MyApp.deleteBook(context, bookId, bookUrl, bookTitle)

                }

            }.show()

    }


    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPdfAdmin(filterList ,this)
        }
        return filter as FilterPdfAdmin

        }
    //ViewHolder class to hold/init UI views for row_pdf_admin.xml
    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Init ui views
        var pdfView: PDFView = binding.pdfView
        var progressBar: ProgressBar = binding.progressBar
        var titleTv: TextView = binding.titleTv
        var descriptionTv: TextView = binding.descriptionTv
        var sizeTv: TextView = binding.sizeTv
        var dateTv: TextView = binding.dateTv
        var categoryTv: TextView = binding.categoryTv
        var moreButton: ImageButton = binding.moreButton

    }


}