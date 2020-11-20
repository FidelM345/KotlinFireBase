package com.example.kotlinfirebase

//the default values given to the below variables is to prevent firebase from crushing
//it is similar to having a default constructor in Java model classes
//the error occurs when retrieving data fro firebase DB

//the variables must have the same name as the attributes defined in the firebaseDB
data class Person (
        val fname:String="",
        val lname:String="",
        val age:Int=0
)