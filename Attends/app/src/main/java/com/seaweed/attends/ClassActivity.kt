package com.seaweed.attends

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
//import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.icu.util.Calendar
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.class_adding_dialog.view.*
import kotlinx.android.synthetic.main.class_entry.view.*
import kotlinx.android.synthetic.main.class_main.*
import kotlinx.android.synthetic.main.student_summary_entry.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
        val courseID= intent.getStringExtra("courseID")
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

        generateQRcode("https://swu-attends.firebaseapp.com/enrollCourse.html?courseKey=$courseID")

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "classesRef - onChildAdded:" + dataSnapshot.key!!)
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
                val key = (dataSnapshot.key).toString()
                val classDate = (dataSnapshot.child("date").value).toString()
                Log.d(TAG, "classesRef - onChildChanged: val ${dataSnapshot.child("studentsList").value}")
//                var a = dataSnapshot.child("studentsList")
                Log.d(TAG, "key: $key")
                updateAttendants("Courses/$courseID/records/$key/studentsList", classDate, courseNumStudents, key, courseID, courseTotalClasses)
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

        //        GridView onClickListener
        gvClasses.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Get the GridView selected/clicked item text
//                        val selectedItem = parent.getItemAtPosition(position).toString()
                val classID = view.tvClassID.text.toString()
                val classDate = view.tvClassDate.text.toString()
                val classAttendants = view.tvClassAttendants.text.toString()
                val classTotalStudents = view.tvClassTotalStudents.text.toString()
                // Display the selected/clicked item text and position on TextView
                Log.d(TAG,"GridView item clicked : $classID - $classAttendants \nAt index position : $position")
                val intent = Intent(this@ClassActivity,ClassSummaryActivity::class.java)
                intent.putExtra("courseID",courseID)
                intent.putExtra("classID",classID)
                intent.putExtra("classDate",classDate)
                intent.putExtra("classAttendants",classAttendants)
                intent.putExtra("classTotalStudents",classTotalStudents)
                startActivity(intent)
            }
        }
//    ------    END GridView onClickListener ------

//        onClickedListener - ADD_CLASS_BTN
        btnAddingClass.setOnClickListener {
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.class_adding_dialog, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("ADDING CLASS")
            //show dialog
            val  mAlertDialog = mBuilder.show()
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDateTime = sdf.format(Date())
            val currentDate = currentDateTime.split(" ")[0]
            val currentTime = currentDateTime.split(" ")[1]
            Log.d(TAG," C DATE is  "+currentDateTime.split(" ")[0]+"\n TIME is: "+currentDateTime.split(" ")[1])
            //login button click of custom layout
            mDialogView.dialogAddClassConfirmBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
                //get text from EditTexts of custom layout
                val d = currentDate.split("/")[0]
                val m = currentDate.split("/")[1]
                val y = currentDate.split("/")[2]
                //set the input text in TextView
                Log.d(TAG, "\nDate: ${mDialogView.dialogDate.text}")
                addingClassConfirm("${mDialogView.dialogDate.text}")
            }

            mDialogView.dialogSetDateClassBtn.setOnClickListener() {
                val d = currentDate.split("/")[0]
                val m = currentDate.split("/")[1]
                val y = currentDate.split("/")[2]
                val datePickerDialog = DatePickerDialog(this@ClassActivity, DatePickerDialog.OnDateSetListener
                { view, year, monthOfYear, dayOfMonth ->
                    var dateInput = "$dayOfMonth-${monthOfYear+1}-$year"
                    mDialogView.dialogDate.setText("$dateInput")
                }, y.toInt(), m.toInt()-1, d.toInt())

                datePickerDialog.show()

            }

            //cancel button click of custom layout
            mDialogView.dialogAddClassCancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }
        }
//        ------ END : onClickedListener - ADD_CLASS_BTN -----
        var classRef = database.getReference("Courses/$courseID/summary/studentsList")
        val classRefEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "classRefEventListener - onChildAdded:" + dataSnapshot.key!!)
                val key = (dataSnapshot.key).toString()
                val attendance = (dataSnapshot.child("attendance").value).toString()
                val name = (dataSnapshot.child("name").value).toString()
                val absence = (courseTotalClasses.toInt() - attendance.toInt()).toString()
//                Log.d(TAG, "classRef key:" + key!!)
//                Log.d(TAG, "classRef shot:" + attendance!!)

                var studentData = StudentSummary( key, name, attendance, absence)
                studentSummaryList.add(studentData)
                studentAdapter = StudentSummaryAdapter(this@ClassActivity, studentSummaryList)

                gvStudentSummary.adapter = studentAdapter
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "classRefEventListener - onChildChanged: ${dataSnapshot.key}")
                var id = dataSnapshot.key
                var value = dataSnapshot.child("attendance").value.toString()
                for ( x in 0 until studentSummaryList.size){
                    if(studentSummaryList[x].id == id){
                        studentSummaryList[x].attend = value
                        studentSummaryList[x].absence = ((courseTotalClasses).toInt() - (value).toInt()).toString()

                        Log.d(TAG, "\nstudentSummaryList[x].attend: ${studentSummaryList[x].attend} \nstudentSummaryList[x].absence: ${studentSummaryList[x].absence}")
                        studentAdapter = StudentSummaryAdapter(this@ClassActivity, studentSummaryList)
                        gvStudentSummary.adapter = studentAdapter
                    }
                }

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
//                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w(TAG, "postComments:onCancelled", databaseError.toException()!!)
                Toast.makeText(baseContext, "Failed to load comments.",
                    Toast.LENGTH_SHORT).show()
            }
        }
        classRef.addChildEventListener(classRefEventListener)

    }

    private fun addingClassConfirm(date: String){
        var database = database.reference
        var courseID= intent.getStringExtra("courseID")
        var classID = database.child("Courses/$courseID/records").push().key

        if (classID == null) {
            Log.w(TAG, "Couldn't get push key for posts")
            return
        }

        database.child("Courses/$courseID/records/$classID/date").setValue(date)
        database.child("Courses/$courseID/summary/totalClasses").addListenerForSingleValueEvent(object :
            ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                var name = dataSnapshot.child("name").value.toString()
                var totalClasses = (dataSnapshot.value).toString().toInt() + 1
                database.child("Courses/$courseID/summary/totalClasses").setValue(totalClasses)
                tvCourseTotalClasses.text = "Total Classes: "+totalClasses

                for (currentIndex in 0 until studentSummaryList.size){
                    studentSummaryList[currentIndex].absence = ((studentSummaryList[currentIndex].absence).toInt() + 1).toString()
                }
                studentAdapter = StudentSummaryAdapter(this@ClassActivity, studentSummaryList)
                gvStudentSummary.adapter = studentAdapter
                return
            }

        })

//        database.child("Courses/$courseID/summary/totalClasses").setValue(date)

        for(x in 0 until studentSummaryList.size){
            database.child("Courses/$courseID/records/$classID/studentsList/${studentSummaryList[x].id}/status").setValue("Absent")
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//                    updateUI(currentUser)
    }

    private fun updateAttendants(classPath: String?, classDate: String, classTotalStudents: String, classID: String, courseID: String, courseTotalClasses: String) {
        var totalAttendants = ""
        var classRef = database.getReference("Courses/$courseID/summary/studentsList")
        var classAttended = database.getReference("$classPath").orderByChild("status").equalTo("Attended")
//        var test = classRef.orderByChild("status").equalTo("Attended")
        database.getReference("$classPath").orderByChild("status").equalTo("Attended").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//        val classAttendedRefEventListener = object : ChildEventListener {
//            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val value = (dataSnapshot.value).toString()
                val key = (dataSnapshot.key).toString()
                val childCount = (dataSnapshot.childrenCount).toString()
                val absences = (classTotalStudents.toLong() - dataSnapshot.childrenCount).toString()
//                Log.d(TAG, "classAttendedRefEventListener: $value")
//                Log.d(TAG, "classAttendedRefEventListener: $key")
//                Log.d(TAG, "classAttendedRefEventListener: $childCount")
//                var classData = ClassData(classID, classDate, childCount, classTotalStudents)
                for ( x in 0 until classesList.size){
                    if(classesList[x].id == classID){
                        classesList[x].attendants = childCount
                    }
                }
                Log.d(TAG, "classesList.size: ${classesList.size}")

                adapter = ClassAdapter(this@ClassActivity, classesList)

                gvClasses.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w(TAG, "postComments:onCancelled", databaseError.toException()!!)
                Toast.makeText(
                    baseContext, "Failed to load comments.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun isClassExit(classID: String): Boolean{
        for ( x in 0 until classesList.size){
            if(classesList[x].id == classID){
                return true
            }
        }
        return false
    }

    private fun queryAttendants(classPath: String?, classDate: String, classTotalStudents: String, classID: String, courseID: String, courseTotalClasses: String) {
        var totalAttendants = ""
        var classRef = database.getReference("Courses/$courseID/summary/studentsList")
        var classAttended = database.getReference("$classPath").orderByChild("status").equalTo("Attended")
//        var test = classRef.orderByChild("status").equalTo("Attended")
        database.getReference("$classPath").orderByChild("status").equalTo("Attended").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//        val classAttendedRefEventListener = object : ChildEventListener {
//            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val value = (dataSnapshot.value).toString()
                val key = (dataSnapshot.key).toString()
                val childCount = (dataSnapshot.childrenCount).toString()
                val absences = (classTotalStudents.toLong() - dataSnapshot.childrenCount).toString()
//                Log.d(TAG, "classAttendedRefEventListener: $value")
//                Log.d(TAG, "classAttendedRefEventListener: $key")
//                Log.d(TAG, "classAttendedRefEventListener: $childCount")
//                var classData = ClassData(classID, classDate, childCount, classTotalStudents)
//                classesList.add(classData)
                if(!isClassExit(classID)){
                    var classData = ClassData(classID, classDate, childCount, classTotalStudents)
                    classesList.add(classData)
                }

                adapter = ClassAdapter(this@ClassActivity, classesList)

                gvClasses.adapter = adapter
            }

//            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "classRefEventListener - onChildChanged: ${dataSnapshot.key}")
//            }
//
//            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
//                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
//            }
//
//            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)
//            }

            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w(TAG, "postComments:onCancelled", databaseError.toException()!!)
                Toast.makeText(
                    baseContext, "Failed to load comments.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
//        }
//        classAttended.addChildEventListener(classAttendedRefEventListener)
//        --- classRef ---
//        var classRef = database.getReference("Courses/$courseID/summary/studentsList")
//        val classRefEventListener = object : ChildEventListener {
//            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
////                Log.d(TAG, "classRefEventListener - onChildAdded:" + dataSnapshot.key!!)
//                val key = (dataSnapshot.key).toString()
//                val attendance = (dataSnapshot.child("attendance").value).toString()
//                val name = (dataSnapshot.child("name").value).toString()
//                val absence = (courseTotalClasses.toInt() - attendance.toInt()).toString()
////                Log.d(TAG, "classRef key:" + key!!)
////                Log.d(TAG, "classRef shot:" + attendance!!)
//                var studentData = StudentSummary( key, name, attendance, absence)
//                studentSummaryList.add(studentData)
//                studentAdapter = StudentSummaryAdapter(this@ClassActivity, studentSummaryList)
//
//                gvStudentSummary.adapter = studentAdapter
//            }
//
//            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
////                Log.d(TAG, "classRefEventListener - onChildChanged: ${dataSnapshot.key}")
//            }
//
//            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
////                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
//            }
//
//            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
////                Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
////                Log.w(TAG, "postComments:onCancelled", databaseError.toException()!!)
//                Toast.makeText(baseContext, "Failed to load comments.",
//                    Toast.LENGTH_SHORT).show()
//            }
//        }
//        classRef.addChildEventListener(classRefEventListener)
//        --END--

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

//    QR Generator
    private fun generateQRcode(path: String){

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(path, BarcodeFormat.QR_CODE, 200, 200)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        qrCourse.setImageBitmap(bitmap)
    }
//    ---- END of QR Generator ----

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
            classView.tvClassDate.text = classData.date!!
            classView.tvClassAttendants.text = "Attended : "+classData.attendants!!
            classView.tvClassTotalStudents.text = "Total Students : "+classData.total!!

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
        studentView.tvStudentTotalID.text = studentData.id!!
        studentView.tvStudentTotalName.text = studentData.name!!
        studentView.tvStudentTotalAttend.text = "Attendances : "+studentData.attend!!
        studentView.tvStudentTotalAbsence.text = "Absences : "+studentData.absence!!

        return studentView
    }
}


    companion object {
        private const val TAG = "EmailPassword"
        val QRcodeWidth = 500
        private val IMAGE_DIRECTORY = "/QRcodeDemonuts"
    }
}
