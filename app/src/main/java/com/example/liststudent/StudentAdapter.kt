package com.example.liststudent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(val students: List<Student>, val onMenuClick: (Int, String) -> Unit) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val id: TextView = view.findViewById(R.id.id)
        val popupMenu: ImageButton = view.findViewById(R.id.more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.student_item, parent, false)
        return StudentViewHolder(adapterLayout)
    }

    override fun getItemCount() = students.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.name.text = student.name
        holder.id.text = student.id
        holder.popupMenu.setOnClickListener{ view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.popup_edit -> {
                        onMenuClick(position, "edit")
                        true
                    }
                    R.id.popup_delete -> {
                        onMenuClick(position, "delete")
                        true
                    }
                    R.id.popup_call -> {
                        onMenuClick(position, "call")
                        true
                    }
                    R.id.popup_email -> {
                        onMenuClick(position, "email")
                        true
                    }
                    else -> false
                }
            }
                popup.show()
        }
    }


}