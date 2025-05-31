package com.example.liststudent

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentAdapter
    private lateinit var editStudentLauncher: ActivityResultLauncher<Intent>
    private lateinit var addStudentLauncher: ActivityResultLauncher<Intent>
    lateinit var db: SQLiteDatabase
    val students = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))


        val dbPath = "${filesDir}/students.db"
        db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tblStudents (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT NOT NULL
            )
        """.trimIndent())

        getAllStudents()

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        editStudentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                val name = it.data?.getStringExtra("name")
                val id = it.data?.getStringExtra("id")
                val phone = it.data?.getStringExtra("phone")
                val email = it.data?.getStringExtra("email")
                val position = it.data?.getIntExtra("pos", -1) ?: -1

                if(!name.isNullOrBlank() && !id.isNullOrBlank() && !phone.isNullOrBlank() && !email.isNullOrBlank() && position >= 0) {
                    val editedStudent = Student(name, id, phone, email)
                    updateStudent(students[position].id, editedStudent)
                    students[position] = editedStudent
                    adapter.notifyItemChanged(position)
                }
            }
        }

        addStudentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                val name = it.data?.getStringExtra("name")
                val id = it.data?.getStringExtra("id")
                val phone = it.data?.getStringExtra("phone")
                val email = it.data?.getStringExtra("email")

                if(!name.isNullOrBlank() && !id.isNullOrBlank() && !phone.isNullOrBlank() && !email.isNullOrBlank()) {
                    val newStudent = Student(name, id, phone, email)
                    insertStudent(newStudent)
                    students.add(0, newStudent)
                    adapter.notifyItemInserted(0)
                }
            }
        }

        adapter = StudentAdapter(students) { position, action ->
            val student = students[position]
            when(action) {
                "edit" -> {
                    val intent =Intent(this, EditStudentActivity::class.java)
                    intent.putExtra("name", student.name)
                    intent.putExtra("id", student.id)
                    intent.putExtra("phone", student.phoneNumber)
                    intent.putExtra("email", student.email)
                    intent.putExtra("pos", position)
                    editStudentLauncher.launch(intent)
                }
                "delete" -> {
                    val dialog =AlertDialog.Builder(this)
                        .setTitle("Delete ${student.name} (${student.id})?")
                        .setMessage("Are you sure about that?")
                        .setPositiveButton("Yes, I sure!") { _, _ ->
                            deleteStudent(students[position].id)
                            students.removeAt(position)
                            adapter.notifyItemRemoved(position)
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss()
                        }
                        .create()
                    dialog.show()
                }
                "call" -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.phoneNumber}"))
                    startActivity(intent)
                }
                else -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${student.email}" +
                            "?subject=${Uri.encode("Test email")}" +
                            "&body=${Uri.encode("Your phone has been hacked!")}")
                    }
                    startActivity(intent)
                }
            }
        }
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_add -> {
                val intent = Intent(this, AddStudentActivity::class.java)
                addStudentLauncher.launch(intent)
            }
        }

        return true
    }

    private fun getAllStudents() {
        students.clear();
        val cursor: Cursor = db.rawQuery("SELECT id,name, phone, email FROM tblStudents", null);
        if(cursor.moveToFirst()) {
            do {
                val id = cursor.getString(0)
                val name = cursor.getString(1)
                val phoneNumber = cursor.getString(2)
                val email = cursor.getString(3)
                students.add(Student(name, id, phoneNumber, email))
            } while(cursor.moveToNext())
        }
        cursor.close()
    }

    private fun insertStudent(student: Student) {
        val newStudent = ContentValues().apply {
            put("id", student.id)
            put("name", student.name)
            put("phone", student.phoneNumber)
            put("email", student.email)
        }
        db.insert("tblStudents", null, newStudent)
    }


    private fun updateStudent(id: String, edittedStudent: Student) {
        val newStudent = ContentValues().apply {
            put("id", edittedStudent.id)
            put("name", edittedStudent.name)
            put("phone", edittedStudent.phoneNumber)
            put("email", edittedStudent.email)
        }
        db.update("tblStudents", newStudent, "id = ?", arrayOf(id))
    }

    private fun deleteStudent(id: String) {
        db.delete("tblStudents", "id =?", arrayOf(id))
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}