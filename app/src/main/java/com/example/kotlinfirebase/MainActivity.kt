package com.example.kotlinfirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    val personTable = Firebase.firestore.collection("person_table")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)






        save.setOnClickListener {


            if (!fname.text.toString().isNullOrEmpty() && !lname.text.toString()
                    .isNullOrEmpty() && !age.text.toString().isNullOrEmpty()
            ) {

                val person = Person(
                    fname.text.toString(),
                    lname.text.toString(),
                    age.text.toString().toInt()
                )


                savePerson(person)

                cleartext()

            } else {

                Toast.makeText(this, "Please fill all the field to save data", Toast.LENGTH_SHORT)
                    .show()
            }


        }


        retrieve.setOnClickListener {
            retrievePerson()
        }


        //  realtimeUpdates()


        update.setOnClickListener {

            val fname1 = fname.text.toString()
            performUpdate(fname1, getUpdatePersonData())
            cleartext()


        }

        delete.setOnClickListener {
            val fname1 = fname.text.toString()

            deleteData(fname1, getUpdatePersonData())
        }


    }


    fun getUpdatePersonData(): Map<String, Any> {

        val map = mutableMapOf<String, Any>()
        val fname = fname.text.toString()
        val lname = lname.text.toString()
        val age = age.text.toString()

        /*     isNullOrEmpty() returns true for a string with no characters and/or zero length as @Wyck commented. It will return false for whitespace.

             isNullOrBlank() returns true for a string with no characters and/or zero length just as*/


        if (!fname.isNullOrBlank()) {
            map["fname"] = fname
        }

        if (!lname.isNullOrBlank()) {
            map["lname"] = lname
        }

        if (!age.isNullOrBlank()) {
            map["age"] = age.toInt()
        }

        return map
    }


    /*fun getOldPersonDataForUpdate():Person{

        val map= mutableMapOf<String, Any>()
        val fname=fname.text.toString()
        val lname=lname.text.toString()
        val age=age.text.toString()


        val person=Person(fname,lname,age)


        return person
    }
*/


    fun deleteData(fname1: String, newPersonMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {


            Log.d("mato", "the fname value is: $fname1")

            if (!fname1.isNullOrBlank()) {

                val documentQueryResults = personTable
                    .whereEqualTo("fname", fname1)
                    /*.whereEqualTo("lname",oldPerson.lname)
                    .whereEqualTo("age",oldPerson.age)*/
                    .get()
                    .await()



                if (!documentQueryResults.isEmpty) {

                    for (doucument in documentQueryResults) {


                        try {

                            personTable.document(doucument.id).delete().await()

                            withContext(Dispatchers.Main) {

                                Toast.makeText(
                                    this@MainActivity,
                                    "Data is successfully deleted",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }

                        } catch (e: Exception) {

                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()

                        }


                    }


                } else {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "The person is not in the database",
                            Toast.LENGTH_SHORT
                        ).show()

                    }


                }

            } else {


                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Enter the name to be deleted",
                        Toast.LENGTH_SHORT
                    ).show()

                }


            }


        }


    fun performUpdate(fname1: String, newPersonMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {


            Log.d("mato", "the fname value is: $fname1")

            if (!fname1.isNullOrBlank()) {

                val documentQueryResults = personTable
                    .whereEqualTo("fname", fname1)
                    /*.whereEqualTo("lname",oldPerson.lname)
                    .whereEqualTo("age",oldPerson.age)*/
                    .get()
                    .await()



                if (!documentQueryResults.isEmpty) {

                    for (doucument in documentQueryResults) {


                        try {

                            personTable.document(doucument.id).set(
                                newPersonMap,
                                SetOptions.merge() //ensures fields that will not be updated will not also be deleted from the DB
                            ).await()

                            withContext(Dispatchers.Main) {

                                Toast.makeText(
                                    this@MainActivity,
                                    "Data is successfully updated",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }

                        } catch (e: Exception) {

                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()

                        }


                    }


                } else {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "The person is not in the database",
                            Toast.LENGTH_SHORT
                        ).show()

                    }


                }

            } else {


                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Enter the name to be changed",
                        Toast.LENGTH_SHORT
                    ).show()

                }


            }


        }


    //get realtime data from the database
    fun realtimeUpdates() {

        personTable.addSnapshotListener { snapshot, error ->

            error?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener//this will terminate the program
            }

            snapshot?.let { it ->

                val sb = StringBuilder()

                for (document in it) {

                    //val person=document.toObject(Person::class.java) can also work with java classed

                    //converting the retrieved documents to objects
                    val person =
                        document.toObject<Person>() //works with firestore-ktx extension only

                    sb.append("$person\n")
                }

                show_data.text =
                    sb.toString() //convert string buffer to string an set it to the text field

            }

        }
    }

    fun retrievePerson() = CoroutineScope(Dispatchers.IO).launch {

        try {
            val querSnapshot = personTable.get().await()
            val sb = StringBuilder()

            for (document in querSnapshot) {

                //val person=document.toObject(Person::class.java) can also work with java classed

                //converting the retrieved documents to objects
                val person = document.toObject<Person>() //works with firestore-ktx extension only

                sb.append("$person\n")
            }

            withContext(Dispatchers.Main) {

                show_data.text =
                    sb.toString() //convert string buffer to string an set it to the text field
            }


        } catch (e: Exception) {

            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()


        }
    }


    fun savePerson(person: Person) {

        CoroutineScope(Dispatchers.IO).launch {
            try {

                personTable.add(person).await()

                withContext(Dispatchers.Main) {

                    Toast.makeText(
                        this@MainActivity,
                        "Successfully saved the data",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    " The error is : ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()

            }


        }


    }

    //clear all edit text fields after data is saved to DB
    fun cleartext() {

        fname.text.clear()
        lname.text.clear()
        age.text.clear()

    }
}