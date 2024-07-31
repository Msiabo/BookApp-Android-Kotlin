package com.example.bookworm.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.bookworm.filters.FilterCategory
import com.example.bookworm.models.ModelCategory
import com.example.bookworm.activities.PdfListAdminActivity
import com.example.bookworm.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase



class AdapterCategory : RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable {

    private val context : Context
    public var categoryArrayList :ArrayList<ModelCategory>
    private var filterList : ArrayList<ModelCategory>

    private var filter : FilterCategory? = null

    private lateinit var binding : RowCategoryBinding

    //Constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>){
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //Inflate/bind row_category.xml file
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false)
        return  HolderCategory(binding.root)
    }

    override fun getItemCount(): Int {
        return  categoryArrayList.size // Number of items in list
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        //Get data
        val model =categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //Set data
        holder.categoryTv.text = category

        //Handle click delete category
        holder.deleteBtn.setOnClickListener {
            //Confirms before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Confirm"){a,d->
                    Toast.makeText(context,"Deleting Category...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model,holder)
                }
                .setNegativeButton("Cancel"){a,d->
                    a.dismiss()
                }
                .show()
        }
        //Handle click,start pdf list admin activity
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity :: class.java)
            intent.putExtra("categoryId" ,id)
            intent.putExtra("category" ,category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        //Get category id of category to be deleted
        val id = model.id

        //Firebase Db > Categories > categoryId
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener{

                Toast.makeText(context, "Category deleted..", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener{e->

                Toast.makeText(context, "Failed to delete category due to ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    //ViewHolder class to hold/init UI views for row_category.xml
    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView){
        //Init ui views
        var categoryTv : TextView = binding.categoryTv
        var deleteBtn : ImageButton = binding.deleteButton

    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterList ,this)
        }
        return  filter as FilterCategory
    }


}