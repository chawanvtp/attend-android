package com.seaweed.attends

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
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.CountDownTimer
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.database.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.active_class_dialog.view.*
import kotlinx.android.synthetic.main.class_entry.*
import kotlinx.android.synthetic.main.class_entry.tvClassAttendants
import kotlinx.android.synthetic.main.class_entry.tvClassDate
import kotlinx.android.synthetic.main.class_entry.tvClassTotalStudents
import kotlinx.android.synthetic.main.class_summary.*
import kotlinx.android.synthetic.main.class_summary_student_entry.*
import kotlinx.android.synthetic.main.class_summary_student_entry.view.*


class ClassSummaryActivity : AppCompatActivity() {
    // DECLARE - Instances
    private lateinit var auth: FirebaseAuth
    var studentsList = ArrayList<Student>()
    val database = FirebaseDatabase.getInstance()
    var studentsRef = database.reference
    var adapter: StudentAdapter? = null
    // inside a basic activity
    private var locationManager : LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.class_summary)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

//        SharedPreferences of LOGIN
//        initLogin()

        val courseID = intent.getStringExtra("courseID")
        val classID=intent.getStringExtra("classID")
        val classDate=intent.getStringExtra("classDate")
        val classAttendants=intent.getStringExtra("classAttendants")
        val classTotalStudents=intent.getStringExtra("classTotalStudents")
//        Log.d(TAG, "classID: $classID")
//        Log.d(TAG, "classDate: $classDate")
//        Log.d(TAG, "classAttendants: $classAttendants")
//        Log.d(TAG, "classTotalStudents: $classTotalStudents")
        tvClassDate.text = classDate
        tvClassAttendants.text = classAttendants
        tvClassTotalStudents.text = classTotalStudents
        generateQRcode("https://swu-attends.firebaseapp.com/classAttend.html?courseKey=$courseID&classKey=$classID")
        studentsRef = database.getReference("Courses/$courseID/records/$classID/studentsList")

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                val key = (dataSnapshot.key).toString()
                val status = (dataSnapshot.child("status").value).toString()
//                Log.d(TAG, "key: $key - status: $status")
//                val classTotalStudents = (dataSnapshot.child("studentsList").childrenCount).toString()
//                val classDate = (dataSnapshot.child("date").value).toString()
//                val studentsList = (dataSnapshot.child("studentsList").value).toString()
//                val studentsAttended = (dataSnapshot.child("studentsList")).toString()
//                queryAttendants("Courses/$courseID/records/$key/studentsList", classDate, courseNumStudents, key, courseID, courseTotalClasses)
//                Log.d(TAG, "REF: Courses/$courseID/summary/studentsList/$key")

                database.getReference("Courses/$courseID/summary/studentsList/$key/name").addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val name = dataSnapshot.value.toString()
                        var studentData = Student(key, name, status, classID, courseID)
//                        Log.d(TAG, "ID: $key | NAME: $name | STATUS: $status")
                        studentsList.add(studentData)
                        adapter = StudentAdapter(this@ClassSummaryActivity, studentsList)

                        gvStudents.adapter = adapter
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
//                        Log.w(TAG, "Failed to read value.", error.toException())
                    }
                })

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
//                Log.d(TAG, "onChildChanged: ${dataSnapshot.value}")
//                Log.d(TAG, "onChildChanged: ${previousChildName}")
                if(dataSnapshot.child("status") !== null){
//                    Log.d(TAG, "statusUpdated: ${dataSnapshot.child("status").value}")
                    updateAttended((dataSnapshot.key).toString(),(dataSnapshot.child("status").value).toString())
                }
//                updateAttended(status)
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
        studentsRef.addChildEventListener(childEventListener)

        //        onClickedListener - ADD_CLASS_BTN
        activeClassBtn.setOnClickListener {
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.active_class_dialog, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("ADDING CLASS")
            //show dialog
            mDialogView.dialogDurationEt.setText("60")
            mDialogView.dialogTimeConsumptionEt.setText("30")

            val  mAlertDialog = mBuilder.show()

//            Log.d(TAG," C DATE is  "+currentDateTime.split(" ")[0]+"\n TIME is: "+currentDateTime.split(" ")[1])
            //login button click of custom layout
            mDialogView.dialogActivateConfirmBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
                var duration = (mDialogView.dialogDurationEt.text).toString()
                var consumption = (mDialogView.dialogTimeConsumptionEt.text).toString()
                //get text from EditTexts of custom layout
                //set the input text in TextView
                Log.d(TAG, "\nDate: ${mDialogView.dialogDurationEt.text} \n" +
                        "Date: ${mDialogView.dialogTimeConsumptionEt.text}")
                activeClass(courseID, classID, duration, consumption)
            }

            //cancel button click of custom layout
            mDialogView.dialogActivateCancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }

            mDialogView.dialogUpdateLocationBtn.setOnClickListener {
                // Create persistent LocationManager reference
//                locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?;
//                //define the listener
//                val locationListener: LocationListener = object : LocationListener {
//                    override fun onLocationChanged(location: Location) {
//                        Log.w(TAG, "" + location.longitude + ":" + location.latitude);
//                    }
//                    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//                    override fun onProviderEnabled(provider: String) {}
//                    override fun onProviderDisabled(provider: String) {}
//                }
//
//                    try {
//                        // Request location updates
//                        locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener);
//                    } catch(ex: SecurityException) {
//                        Log.d("myTag", "Security Exception, no location available");
//                    }
            }

        }
//        ------ END : onClickedListener - ADD_CLASS_BTN -----

    }

    private fun activeClass(courseID: String, classID: String, duration: String, consumption: String){
        var database = database.reference
        database.child("Courses/$courseID/activation/consumption").setValue(consumption)
        database.child("Courses/$courseID/activation/currentClass").setValue(classID)

        val timer = object: CountDownTimer((duration.toLong()*1000), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d(TAG, "sec: "+ millisUntilFinished.toString())
                activeClassBtn.text = "remaining: "+ (millisUntilFinished/1000).toString()
                activeClassBtn.setBackgroundColor(Color.parseColor("#DC143C"))
            }

            override fun onFinish() {
                database.child("Courses/$courseID/activation/consumption").setValue(99999)
                database.child("Courses/$courseID/activation/currentClass").setValue("none")
                activeClassBtn.text = "ACTIVE CLASS"
                activeClassBtn.setBackgroundColor(Color.parseColor("#33b5e5"))
            }

        }
        timer.start()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//                    updateUI(currentUser)
    }

    public fun signInWithEmail(email: String?, password: String?){
        auth.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }

    private fun updateAttended(id: String, status: String){
        var attended = (tvClassAttendants.text).toString()
        attended = attended.substring(11, attended.length)
//        Log.d(TAG, "updateAttended : $id => $attended")
        updateStudentList(id,status)
        if(status == "Attended"){
            tvClassAttendants.text = "Attended : " + ( ((attended)).toInt() + 1 ).toString()
            updateSummary(id, 1)
        }else{
            tvClassAttendants.text = "Attended : " + ( ((attended)).toInt() - 1 ).toString()
            updateSummary(id, -1)
        }
    }

    private fun updateSummary(id: String, attendanceValue: Int){
        val courseID = intent.getStringExtra("courseID")
        val classID=intent.getStringExtra("classID")

        database.getReference("Courses/$courseID/summary/studentsList/$id").addListenerForSingleValueEvent(object :
            ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var name = dataSnapshot.child("name").value.toString()
                var attendance = ((dataSnapshot.child("attendance").value).toString().toInt() + attendanceValue).toString()
                database.reference.child("Courses/$courseID/summary/studentsList/$id/name").setValue(name)
                database.reference.child("Courses/$courseID/summary/studentsList/$id/attendance").setValue(attendance)
                return
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
//                        Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }

    private fun updateStudentList(id: String, status: String){
        for (x in 0 until this.studentsList.size){
            if(this.studentsList[x].id == id){
                this.studentsList[x].status = status
//                Log.d(TAG, "updateStudentList : ${this.studentsList[x].id} => ${this.studentsList[x].status}")
                adapter = StudentAdapter(this@ClassSummaryActivity, studentsList)
                gvStudents.adapter = adapter
            }
        }
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
        qrClass.setImageBitmap(bitmap)
    }
//    ---- END of QR Generator ----

    private fun updateUI(user: FirebaseUser?){
        if(user != null){
//            Log.d(TAG, user.uid.toString())
            Toast.makeText(baseContext, user.uid.toString(),
                Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        } else {
//            Log.d(TAG, "Login Failed.")
        }
        Toast.makeText(baseContext, "updateUI.",
            Toast.LENGTH_SHORT).show()
//        Log.d(TAG, "updateUI.")
    }

    private  fun createLoginSession(user: FirebaseUser?, password: String?){
        val editor = getSharedPreferences("myPref", Context.MODE_PRIVATE).edit()
        val reader = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        editor.putString("email", user?.email.toString())
        editor.putString("id", (user?.uid).toString())
        editor.putString("password", password)
        editor.commit();
        val email = reader.getString("email", "-1")
        Toast.makeText(baseContext, user?.email+" : ",
            Toast.LENGTH_SHORT).show()
    }

    private fun initLogin(){
        val reader = getSharedPreferences("myPref", Context.MODE_PRIVATE)
        val email = reader.getString("email", "-1")
        val password = reader.getString("password", "-1")
//        Toast.makeText(baseContext, "IniLogin : "+email+" , "+password,
//            Toast.LENGTH_SHORT).show()
        if(email != "-1" || password != "-1"){
            signInWithEmail(email, password)
        }
    }

    class StudentAdapter : BaseAdapter {
        var studentList = ArrayList<Student>()
        var context: Context? = null
        var status_list = arrayListOf("Attended", "Absent")

        constructor(context: Context, studentList: ArrayList<Student>) : super() {
            this.context = context
            this.studentList = studentList
        }

        override fun getCount(): Int {
            return studentList.size
        }

        override fun getItem(position: Int): Any {
            return studentList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val studentData = this.studentList[position]
            val database = FirebaseDatabase.getInstance()
            val studentList = this.studentList
            val studentDataPosition = position
//            Log.d(TAG, "ID: "+studentData.id)
//            Log.d(TAG, "Name: "+studentData.name)
//            Log.d(TAG, "Status: "+studentData.status)
            var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var studentView = inflator.inflate(R.layout.class_summary_student_entry, null)
            studentView.tvStudentID.text = studentData.id!!
            studentView.tvStudentName.text = studentData.name!!
            studentView.spinnerStudentStatus.setSelection(getSpinnerPosition(studentData.status))
            if(studentData.status == "Attended"){
                studentView.setBackgroundColor(Color.parseColor("#90EE90"))
            }
            studentView.spinnerStudentStatus?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                    Log.d(TAG, "ID: "+studentData.id)
//                    Log.d(TAG, "Name: "+studentData.name)
//                    Log.d(TAG, "Status: "+status_list[position])
//                    Log.d(TAG, "classID: "+studentData.classID)
//                    Log.d(TAG, "courseID: "+studentData.courseID)
                    studentList[studentDataPosition].status = status_list[position]
                    database.getReference("Courses/${studentData.courseID}/records/${studentData.classID}/studentsList/${studentData.id}/status").setValue(status_list[position])
//                    database.getReference("Courses/${studentData.courseID}/summary/studentsList/${studentData.id}/status").setValue(status_list[position])
                }

            }

            return studentView
        }

        private fun getSpinnerPosition(status: String): Int {
            if(status == "Attended"){
                return 0
            }else if(status == "Absent"){
                return 1
            }
                return 2
        }

        private fun getAttendanceByID(courseID: String, id: String){
//            var database = FirebaseDatabase.getInstance()
//            database.getReference("Courses/$courseID/summary/studentsList/$id/name").addValueEventListener(object :
//                ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    val name = dataSnapshot.value.toString()
//                    var studentData = Student(key, name, status, classID, courseID)
////                        Log.d(TAG, "ID: $key | NAME: $name | STATUS: $status")
//                    studentsList.add(studentData)
//                    adapter = StudentAdapter(this@ClassSummaryActivity, studentsList)
//
//                    gvStudents.adapter = adapter
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Failed to read value
////                        Log.w(TAG, "Failed to read value.", error.toException())
//                }
//            })
        }

    }


    companion object {
        private const val TAG = "EmailPassword"
    }
}
