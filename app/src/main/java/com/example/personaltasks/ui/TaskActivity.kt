package com.example.personaltasks.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.personaltasks.R
import com.example.personaltasks.databinding.ActivityTaskBinding
import com.example.personaltasks.model.Constants.EXTRA_TASK
import com.example.personaltasks.model.Constants.EXTRA_VIEW_TASK
import com.example.personaltasks.model.Task
import java.text.SimpleDateFormat
import java.util.Locale

class TaskActivity : AppCompatActivity() {
    private val acb: ActivityTaskBinding by lazy {
        ActivityTaskBinding.inflate(layoutInflater)
    }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(acb.root)

        val toolbarIcon = findViewById<ImageView>(R.id.toolbar_icon)
        toolbarIcon.visibility = View.GONE

        setSupportActionBar(acb.toolbarIn.toolbar)

        acb.deadlineEt.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    this@TaskActivity,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        calendar.set(selectedYear, selectedMonth, selectedDay)
                        setText(dateFormat.format(calendar.time))
                    },
                    year, month,day
                )
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()
            }
        }
        val receivedTask = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TASK, Task::class.java)
        } else intent.getParcelableExtra<Task>(EXTRA_TASK)

        receivedTask?.let{
            with(acb) {
                titleEt.setText(it.title)
                descriptionEt.setText(it.description)
                deadlineEt.setText(it.deadline)

                val viewTask = intent.getBooleanExtra(EXTRA_VIEW_TASK, false)
                if(viewTask) run {
                    titleEt.isEnabled = false
                    descriptionEt.isEnabled = false
                    deadlineEt.isEnabled = false

                    saveBt.visibility = View.GONE
                    cancelBt.visibility = View.VISIBLE

                    cancelBt.setOnClickListener {
                        val intent = Intent(this@TaskActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        with(acb) {
            saveBt.setOnClickListener {
                val title = titleEt.text.toString().trim()
                val description = descriptionEt.text.toString().trim()

                if(title.isEmpty()) {
                    titleEt.error = "Titulo é obrigatório!"
                    titleEt.requestFocus()
                    return@setOnClickListener
                }

                if(description.isEmpty()) {
                    descriptionEt.error = "Descrição é obrigatória!"
                    descriptionEt.requestFocus()
                    return@setOnClickListener
                }

                val task = Task (
                    receivedTask?.id?:hashCode(),
                    titleEt.text.toString(),
                    descriptionEt.text.toString(),
                    deadlineEt.text.toString()
                )

                val resultIntent = Intent().apply {
                    putExtra(EXTRA_TASK, task)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            cancelBt.setOnClickListener {
                val intent = Intent(this@TaskActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}