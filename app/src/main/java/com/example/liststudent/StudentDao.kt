package com.example.liststudent

import androidx.room.*

@Dao
interface StudentDao {
    @Query("SELECT * FROM tblStudents")
    suspend fun getAll(): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student)

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)
}
