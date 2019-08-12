package com.seaweed.attends

class Student {
    var id: String = ""
    var name: String = ""
    var status: String = ""
    var classID: String = ""
    var courseID: String = ""

    constructor()

    constructor(id: String, name: String, status: String, classID: String, courseID: String){
        this.id = id
        this.name = name
        this.status = status
        this.classID = classID
        this.courseID = courseID
    }

}