package com.seaweed.attends

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.home_main.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.course_adding_dialog.view.*
import kotlinx.android.synthetic.main.course_entry.view.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity() {
    // DECLARE - Instances
    private lateinit var auth: FirebaseAuth
    val database = FirebaseDatabase.getInstance()
//    val coursesRef = database.getReference("Courses/-LilWkqoSN4ytIauvrwE/records")
    var coursesRef = database.reference
    //  -- END --

//    var tableView = findViewById(R.id.table_home) as TableLayout
    private lateinit var listView: ListView
    var adapter: CourseAdapter? = null
    var coursesList = ArrayList<Course>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_main)


//                var coursesList = ArrayList<Course>()
                // Initialize Firebase Auth
                auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                if(currentUser != null){
                    Log.d(TAG, "currentUser: "+currentUser.uid)
                }
        coursesRef = database.getReference("Lecturers/${(currentUser!!.uid).toString()}/myCourses")

//        val editor = getSharedPreferences("myPref", Context.MODE_PRIVATE).edit()
        val reader = getSharedPreferences("myPref", Context.MODE_PRIVATE)
//        editor.commit();
        val id = reader.getString("name", "-1")
        Toast.makeText(baseContext, ""+id,
            Toast.LENGTH_SHORT).show()

//  Database functions
//                coursesRef.addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        // This method is called once with the initial value and again
//                        // whenever data at this location is updated.
//                        val value = dataSnapshot.getValue(String::class.java)
//                        Log.d(TAG, "Value is: $value")
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        // Failed to read value
//                        Log.w(TAG, "Failed to read value.", error.toException())
//                    }
//                })
                val childEventListener = object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                        Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                        val shot = (dataSnapshot.value).toString()
                        queryCourseByID(shot)
//                        var course = Course(shot, "a", "b", "c")
//                        coursesList.add(course)
                        Log.d(TAG, "onChildAdded coursesList:" + coursesList.size)
//                        adapter = CourseAdapter(this@HomeActivity, coursesList)
//
//                        gvCourses.adapter = adapter
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                        Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                        Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                        Toast.makeText(baseContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                coursesRef.addChildEventListener(childEventListener)
//  --- END of database functions ---

//        GridView onClickListener
                gvCourses.onItemClickListener = object : AdapterView.OnItemClickListener {
                    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        // Get the GridView selected/clicked item text
//                        val selectedItem = parent.getItemAtPosition(position).toString()
                        val courseID = view.tvCourseID.text.toString()
                        val courseName = view.tvCourseName.text.toString()
                        val courseNumStudents = (view.tvCourseNumStudents.text.toString()).substring(11)
                        val courseTotalClasses = (view.tvCourseTotalClasses.text.toString()).substring(16)
                        // Display the selected/clicked item text and position on TextView
                        Log.d(TAG,"GridView item clicked : $courseID \nAt index position : $position")
                        val intent = Intent(this@HomeActivity,ClassActivity::class.java)
                        intent.putExtra("courseID",courseID)
                        intent.putExtra("courseName",courseName)
                        intent.putExtra("courseNumStudents",courseNumStudents)
                        intent.putExtra("courseTotalClasses",courseTotalClasses)
                        startActivity(intent)
                    }
                }
//    ------    END GridView onClickListener ------

        //------ ADD COURSE - click to show dialog ------
        addCourseBtn.setOnClickListener {
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.course_adding_dialog, null)
            //AlertDialogBuilder
            val mBuilder = android.support.v7.app.AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("ADD COURSE")
            //show dialog
            val  mAlertDialog = mBuilder.show()
            //login button click of custom layout
            mDialogView.dialogAddCourseConfirmBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
                //get text from EditTexts of custom layout
                val name = mDialogView.dialogNameEt.text.toString()
                val sec = mDialogView.dialogSectionEt.text.toString()
                val semester = mDialogView.dialogSemesterEt.text.toString()
                if (currentUser != null && name != "" && sec != "" && semester != "") {
                    addCourseBtnClicked(currentUser.uid, name, sec, semester)
                }
                //set the input text in TextView
                Log.d(TAG, "Name:"+ name +"\nSec: "+ sec +"\nSemester: "+ semester)
            }
            //cancel button click of custom layout
            mDialogView.dialogAddCourseCancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }
        }
        //------ END == ADD COURSE - click to show dialog ------
    }

    private fun addCourseBtnClicked(id: String, name: String, sec: String, semester: String){
        val database = database.reference
        val newCourseKey = database.child("Courses").push().key
        if (newCourseKey != null) {
            database.child("Courses").child(newCourseKey).child("section").setValue(sec)
            database.child("Courses").child(newCourseKey).child("name").setValue("$semester  |  $name")
            database.child("Courses").child(newCourseKey).child("summary/totalClasses").setValue(0)
            database.child("Courses").child(newCourseKey).child("activation/consumption").setValue(999999)
            database.child("Courses").child(newCourseKey).child("activation/currentClass").setValue("none")
            database.child("Courses").child(newCourseKey).child("activation/latitude").setValue(0)
            database.child("Courses").child(newCourseKey).child("activation/longitude").setValue(0)
            database.child("Lecturers/$id/myCourses").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var count = (dataSnapshot.childrenCount).toString()
                    database.child("Lecturers/$id/myCourses/$count").setValue(newCourseKey)
                    Log.d(TAG, count)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(baseContext, "Failed to load comments.",Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun queryCourseByID(courseID: String?){
        var courseName = ""
        var courseNumStudents = ""
        var courseTotalClasses = ""

        database.getReference("Courses/$courseID/name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                courseName = value.toString()
                Log.d(TAG, "courseName is: $courseName")

                database.getReference("Courses/$courseID/summary/totalClasses").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val value = dataSnapshot.value
                        courseTotalClasses = value.toString()
                        Log.d(TAG, "courseTotalClasses is: $courseTotalClasses")

                        database.getReference("Courses/$courseID/summary/studentsList").addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val value = dataSnapshot.childrenCount
                                courseNumStudents = value.toString()
                                Log.d(TAG, "courseNumStudents is: $courseNumStudents")

                                //        ADD Courses from query
                                var course = Course( courseID.toString(), courseName, courseNumStudents, courseTotalClasses)
                                var isExist = false
                                for (currentIndex in 0 until coursesList.size){
                                    if(coursesList[currentIndex].id === course.id){
                                        coursesList[currentIndex] = course
                                        isExist = true
                                    }
                                }

                                if(!isExist){
                                    coursesList.add(course)
                                }

                                Log.d(TAG, "onChildAdded coursesList:" + coursesList.size!!)
                                adapter = CourseAdapter(this@HomeActivity, coursesList)

                                gvCourses.adapter = adapter
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.w(TAG, "Failed to read value.", error.toException())
                            }
                        })

                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException())
                    }
                })

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private  fun getLoginSession(user: FirebaseUser?, password: String?){
//        val editor = getSharedPreferences("myPref", Context.MODE_PRIVATE).edit()
        val reader = getSharedPreferences("myPref", Context.MODE_PRIVATE)
//        editor.putString("email", user?.email.toString())
//        editor.putString("id", (user?.uid).toString())
//        editor.putString("password", password)
//        editor.commit();
        val email = reader.getString("email", "-1")
        Toast.makeText(baseContext, user?.email+" : ",
            Toast.LENGTH_SHORT).show()
    }

    class CourseAdapter : BaseAdapter {
        var courseList = ArrayList<Course>()
        var context: Context? = null

        constructor(context: Context, courseList: ArrayList<Course>) : super() {
            this.context = context
            this.courseList = courseList
        }

        override fun getCount(): Int {
            return courseList.size
        }

        override fun getItem(position: Int): Any {
            return courseList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val course = this.courseList[position]

            var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var courseView = inflator.inflate(R.layout.course_entry, null)
            courseView.tvCourseID.text = course.id!!
            courseView.tvCourseName.text = course.name!!
            courseView.tvCourseNumStudents.text = "Students : " + course.numStudents!!
            courseView.tvCourseTotalClasses.text = "Total Classes : " + course.total!!

            return courseView
        }
    }


    companion object {
        private const val TAG = "EmailPassword"
    }
}