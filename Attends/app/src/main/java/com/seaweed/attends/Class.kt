package com.seaweed.attends

class ClassData {
    var id: String = ""
    var attendants: String = ""
    var date: String = ""
    var total: String = ""

    constructor()

    constructor(id: String, date: String, attendants: String, total: String){
        this.id = id
        this.date = date
        this.attendants = attendants
        this.total = total
    }

}