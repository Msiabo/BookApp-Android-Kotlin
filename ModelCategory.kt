package com.example.bookworm.models

class ModelCategory {

    //Variables must match firebase db variables
    var category:String = ""
    var id:String = ""
    var timestamp : Long = 0
    var uid : String = ""

    //Empty constructor required by firebase
    constructor()

    //Parameterized constructor
    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }


}