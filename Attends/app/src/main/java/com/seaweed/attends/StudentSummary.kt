package com.seaweed.attends

class StudentSummary {
    var name: String = ""
    var attend: String = ""
    var absence: String = ""

    constructor()

    constructor(name: String, attend: String, absence: String){
        this.name = name
        this.attend = attend
        this.absence = absence
    }

}