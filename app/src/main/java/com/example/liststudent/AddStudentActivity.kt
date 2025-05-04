package com.example.liststudent

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddStudentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_student)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editName: EditText = findViewById(R.id.edit_name)
        val editId: EditText = findViewById(R.id.edit_id)
        val editPhone: EditText = findViewById(R.id.edit_phone_number)
        val editEmail: EditText = findViewById(R.id.edit_email)
        val confirmBtn: Button = findViewById(R.id.confirm_btn)
        val cancelBtn: Button = findViewById(R.id.cancel_btn)

        confirmBtn.setOnClickListener {
            intent.putExtra("name", editName.text.toString())
            intent.putExtra("id", editId.text.toString())
            intent.putExtra("phone", editPhone.text.toString())
            intent.putExtra("email", editEmail.text.toString())
            setResult(RESULT_OK, intent)
            finish()
        }

        cancelBtn.setOnClickListener { finish() }
    }
}