package com.seaweed.attends

class Course {
    var id: String = ""
    var name: String = ""
    var numStudents: String = ""
    var total: String = ""

    constructor()

    constructor(id: String, name: String, numStudents: String, total: String){
        this.id = id
        this.name = name
        this.numStudents = numStudents
        this.total = total
    }

}