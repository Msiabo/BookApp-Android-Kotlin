package com.example.bookworm.filters

import android.widget.Filter
import com.example.bookworm.adapters.AdapterPdfUser
import com.example.bookworm.models.ModelPdf

/*Used to filter data from recyclerview | search pdf from pdf list in recyclerview*/
class FilterPdfUser  : Filter {
    //Arraylist in which we want to search
    var filterList : ArrayList<ModelPdf>
    //Adapter in which we want to implement
    var adapterPdfUser : AdapterPdfUser

    //Constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser){
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }



    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint : CharSequence? = constraint
        val results = FilterResults()

        //Value to be searched should not be null or empty
        if(constraint != null && constraint.isNotEmpty()){
            //Change to uppercase or lowercase to avoid case sensitivity
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for(i in filterList.indices){

                if(filterList[i].title.lowercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //Searched value is null or empty ,return all data
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //Apply filter changes
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        adapterPdfUser.notifyDataSetChanged()
    }
}