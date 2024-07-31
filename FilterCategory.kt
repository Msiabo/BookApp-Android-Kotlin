package com.example.bookworm.filters

import  android.widget.Filter
import com.example.bookworm.adapters.AdapterCategory
import com.example.bookworm.models.ModelCategory


class FilterCategory : Filter{

    //ArrayList we want to search
    private var filterList : ArrayList<ModelCategory>

    //Adapter in which filter needs to be implemented
    private var adapterCategory : AdapterCategory

    //Constructor
    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //Value should not be null & not empty
        if(constraint != null && constraint.isNotEmpty()){

            //Change to upper case,or lower case to avoid case sensitivity
            constraint = constraint.toString().uppercase()
            val filteredModels : ArrayList<ModelCategory> = ArrayList()
            for(i in 0 until filterList.size){

                //Validate
                if(filterList[i].category.uppercase().contains(constraint)){
                    //Add to filtered list
                    filteredModels.add(filteredModels[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //Search value is null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return  results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //Apply filter changes
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        //Notify changes
        adapterCategory.notifyDataSetChanged()
    }

}