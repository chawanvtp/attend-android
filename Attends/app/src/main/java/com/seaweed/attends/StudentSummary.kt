package com.seaweed.attends

class StudentSummary {
    var id: String = ""
    var name: String = ""
    var attend: String = ""
    var absence: String = ""

    constructor()

    constructor(id: String, name: String, attend: String, absence: String){
        this.id = id
        this.name = name
        this.attend = attend
        this.absence = absence
    }

}