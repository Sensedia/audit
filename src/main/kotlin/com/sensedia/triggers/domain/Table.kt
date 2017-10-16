package com.sensedia.triggers.domain

class Table(name: String){
    var name: String = name
    lateinit var fields: HashMap<String, String>

    override fun toString(): String {
        return "Table(fields=$fields, name=$name)"
    }

}