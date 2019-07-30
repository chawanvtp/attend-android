package com.seaweed.attends

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.class_entry.view.*
import kotlinx.android.synthetic.main.class_main.*
import kotlinx.android.synthetic.main.student_summary_entry.view.*


class ClassActivity : AppCompatActivity() {
    // DECLARE - Instances
    private lateinit var auth: FirebaseAuth
    var classesList = ArrayList<ClassData>()
    var studentSummaryList = ArrayList<StudentSummary>()
    val database = FirebaseDatabase.getInstance()
    var classesRef = database.reference
    var adapter: ClassAdapter? = null
    var studentAdapter: StudentSummaryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.class_main)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val courseID=intent.getStringExtra("courseID")
        val courseName=intent.getStringExtra("courseName")
        val courseNumStudents=intent.getStringExtra("courseNumStudents")
        val courseTotalClasses=intent.getStringExtra("courseTotalClasses")
//        Log.d(TAG, "courseID: $courseID")
//        Log.d(TAG, "courseName: $courseName")
//        Log.d(TAG, "courseNumStudents: $courseNumStudents")
//        Log.d(TAG, "courseTotalClasses: $courseTotalClasses")
        tvCourseName.text = courseName
        tvCourseTotalClasses.text = "Total Classes: "+courseTotalClasses
        tvCourseNumStudents.text = "Students: "+courseNumStudents
        classesRef = database.getReference("Courses/$courseID/records")

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                val key = (dataSnapshot.key).toString()
                val shot = (dataSnapshot.value).toString()
                val classTotalStudents = (dataSnapshot.child("studentsList").childrenCount).toString()
                val classDate = (dataSnapshot.child("date").value).toString()
                val studentsList = (dataSnapshot.child("studentsList").value).toString()
                val studentsAttended = (dataSnapshot.child("studentsList")).toString()
                queryAttendants("Courses/$courseID/records/$key/studentsList", classDate, courseNumStudents, key, courseID, courseTotalClasses)
//                        var classData = ClassData(classDate, "attendants", "absences", classTotalStudents)
//                        classesList.add(classData)
//                        adapter = ClassAdapter(this@ClassActivity, classesList)
//
//                        gvClasses.adapter = adapter
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
                Log.w(TAG, "postComments:onCancelled", databaseError.toException()!!)
                Toast.makeText(baseContext, "Failed to load comments.",
                    Toast.LENGTH_SHORT).show()
            }
        }
        classesRef.addChildEventListener(childEventListener)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//                    updateUI(currentUser)
    }

    private fun queryAttendants(classPath: String?, classDate: String, classTotalStudents: String, classID: String, courseID: String, courseTotalClasses: String) {
        var totalAttendants = ""

        var classRef = database.getReference("Courses/$courseID/summary/studentsList")
//        var test = classRef.orderByChild("status").equalTo("Attended")
        database.getReference("$classPath").orderByChild("status").equalTo("Attended").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = (dataSnapshot.value).toString()
                val key = (dataSnapshot.key).toString()
                val childCount = (dataSnapshot.childrenCount).toString()
                val absences = (classTotalStudents.toLong() - dataSnapshot.childrenCount).toString()
                Log.d(TAG, "value is: $value")
                Log.d(TAG, "key is: $key")
                Log.d(TAG, "childCount is: $childCount")
                var classData = ClassData( classID, classDate, childCount, classTotalStudents )
                classesList.add(classData)
                adapter = ClassAdapter(this@ClassActivity, classesList)

                gvClasses.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        val classRefEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                val key = (dataSnapshot.key).toString()
                val attendance = (dataSnapshot.child("attendance").value).toString()
                val absence = (courseTotalClasses.toInt() - attendance.toInt()).toString()
//                Log.d(TAG, "classRef key:" + key!!)
//                Log.d(TAG, "classRef shot:" + attendance!!)
                var studentData = StudentSummary( key, attendance, absence)
                studentSummaryList.add(studentData)
                studentAdapter = StudentSummaryAdapter(this@ClassActivity, studentSummaryList)

                gvStudentSummary.adapter = studentAdapter
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
                Log.w(TAG, "postComments:onCancelled", databaseError.toException()!!)
                Toast.makeText(baseContext, "Failed to load comments.",
                    Toast.LENGTH_SHORT).show()
            }
        }
        classRef.addChildEventListener(classRefEventListener)
//        database.getReference("$classPath").addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val value = (dataSnapshot.value).toString()
//                val key = (dataSnapshot.key).toString()
////                courseName = value.toString()
//                Log.d(TAG, "value is: $value")
//                Log.d(TAG, "key is: $key")
//
////                database.getReference("$classPath/$key/s").addValueEventListener(object : ValueEventListener {
////                    override fun onDataChange(dataSnapshot: DataSnapshot) {
////                        val value = dataSnapshot.getValue(String::class.java)
////                        Log.d(TAG, "courseName is: $value")
////
////                    }
////
////                    override fun onCancelled(error: DatabaseError) {
////                        // Failed to read value
////                        Log.w(TAG, "Failed to read value.", error.toException())
////                    }
////                })
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })
    }


    class ClassAdapter : BaseAdapter {
        var classList = ArrayList<ClassData>()
        var context: Context? = null

        constructor(context: Context, classList: ArrayList<ClassData>) : super() {
            this.context = context
            this.classList = classList
        }

        override fun getCount(): Int {
            return classList.size
        }

        override fun getItem(position: Int): Any {
            return classList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val classData = this.classList[position]

            var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var classView = inflator.inflate(R.layout.class_entry, null)
            classView.tvClassID.text = classData.id!!
            classView.tvClassAttendants.text = classData.date!!
            classView.tvClassAbsences.text = "ATTENDANTS: "+classData.attendants!!
            classView.tvClassTotalStudents.text = "TOTAL STUDENTS: "+classData.total!!

            return classView
        }
    }

//    Student Summary Adaper
class StudentSummaryAdapter : BaseAdapter {
    var studentSummaryList = ArrayList<StudentSummary>()
    var context: Context? = null

    constructor(context: Context, studentSummaryList: ArrayList<StudentSummary>) : super() {
        this.context = context
        this.studentSummaryList = studentSummaryList
    }

    override fun getCount(): Int {
        return studentSummaryList.size
    }

    override fun getItem(position: Int): Any {
        return studentSummaryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val studentData = this.studentSummaryList[position]

        var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var studentView = inflator.inflate(R.layout.student_summary_entry, null)
        studentView.tvStudentTotalName.text = studentData.name!!
        studentView.tvStudentTotalAttend.text = "attend: "+studentData.attend!!
        studentView.tvStudentTotalAbsence.text = "absence: "+studentData.absence!!

        return studentView
    }
}


    companion object {
        private const val TAG = "EmailPassword"
    }
}
