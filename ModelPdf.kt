package com.example.bookworm.models

import com.example.bookworm.adapters.AdapterBookFavourites

class ModelPdf {

    //Declare variables
    var uid : String = ""
    var id : String = ""
    var title : String = ""
    var description : String = ""
    var categoryId : String = ""
    var url : String =""
    var timestamp : Long = 0
    var viewsCount : Long = 0
    var downloadsCount : Long = 0
    var isFavourite  : Boolean = false

    //Empty constructor
    constructor()
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        timestamp: Long,
        viewsCount: Long,
        downloadsCount: Long,
        isFavourite: Boolean
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
        this.isFavourite = isFavourite
    }


}