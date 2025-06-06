package com.example.liststudent

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tblStudents")
data class Student(@PrimaryKey(autoGenerate = true) val key: Int, val name: String, val id: String, val phoneNumber: String, val email: String)
