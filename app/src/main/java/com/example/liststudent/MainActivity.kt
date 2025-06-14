package com.example.liststudent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StudentAdapter
    private lateinit var editStudentLauncher: ActivityResultLauncher<Intent>
    private lateinit var addStudentLauncher: ActivityResultLauncher<Intent>
    val students = mutableListOf<Student>()
    private lateinit var studentDao: StudentDao

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

        studentDao = StudentDatabase.getInstance(this).studentDao()
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
                val key = it.data?.getIntExtra("key", 0) ?: 0
                if(!name.isNullOrBlank() && !id.isNullOrBlank() && !phone.isNullOrBlank() && !email.isNullOrBlank() && position >= 0) {
                    val editedStudent = Student(key, name, id, phone, email)
                    updateStudent(editedStudent)
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
                    val newStudent = Student(key = 0, name, id, phone, email)
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
                    intent.putExtra("key", student.key)
                    editStudentLauncher.launch(intent)
                }
                "delete" -> {
                    val dialog =AlertDialog.Builder(this)
                        .setTitle("Delete ${student.name} (${student.id})?")
                        .setMessage("Are you sure about that?")
                        .setPositiveButton("Yes, I sure!") { _, _ ->
                            deleteStudent(students[position])
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
        lifecycleScope.launch {
            students.clear()
            students.addAll(studentDao.getAll())
            adapter.notifyDataSetChanged()
        }
    }

    private fun insertStudent(student: Student) {
        lifecycleScope.launch {
            studentDao.insert(student)
        }
    }


    private fun updateStudent(edittedStudent: Student) {
        lifecycleScope.launch {
            studentDao.update(edittedStudent)
        }
    }

    private fun deleteStudent(student: Student) {
        lifecycleScope.launch {
            studentDao.delete(student)
        }
    }
}