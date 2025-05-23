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
    // View Binding para acesso seguro e direto aos elementos da interface
    private val acb: ActivityTaskBinding by lazy {
        ActivityTaskBinding.inflate(layoutInflater)
    }

    // Formato para data no padrão "dd/MM/yyyy"
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Instância do calendário para manipulação da data escolhida
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(acb.root)

        // Esconde o ícone da toolbar nessa activity (não usado aqui)
        val toolbarIcon = findViewById<ImageView>(R.id.toolbar_icon)
        toolbarIcon.visibility = View.GONE

        // Configura a toolbar customizada
        setSupportActionBar(acb.toolbarIn.toolbar)

        // Configura o campo de data para abrir um DatePickerDialog ao ser clicado
        acb.deadlineEt.apply {
            isFocusable = false  // impede edição manual
            isClickable = true   // permite clicar para abrir o seletor de data
            setOnClickListener {
                // Pega a data atual para inicializar o DatePicker
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                // Cria o diálogo de seleção de data
                val datePickerDialog = DatePickerDialog(
                    this@TaskActivity,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        // Atualiza o calendário com a data selecionada
                        calendar.set(selectedYear, selectedMonth, selectedDay)
                        // Atualiza o texto do EditText com a data formatada
                        setText(dateFormat.format(calendar.time))
                    },
                    year, month, day
                )
                // Define que a data mínima selecionável é a data atual
                datePickerDialog.datePicker.minDate = System.currentTimeMillis()
                datePickerDialog.show()
            }
        }

        // Recupera a tarefa passada pela intent, respeitando versões do Android
        val receivedTask = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TASK, Task::class.java)
        } else intent.getParcelableExtra<Task>(EXTRA_TASK)

        // Se uma tarefa foi recebida, popula os campos com seus dados
        receivedTask?.let {
            with(acb) {
                titleEt.setText(it.title)
                descriptionEt.setText(it.description)
                deadlineEt.setText(it.deadline)

                // Verifica se a activity foi aberta apenas para visualização
                val viewTask = intent.getBooleanExtra(EXTRA_VIEW_TASK, false)
                if(viewTask) run {
                    // Desabilita edição dos campos para modo visualização
                    titleEt.isEnabled = false
                    descriptionEt.isEnabled = false
                    deadlineEt.isEnabled = false

                    // Oculta botão salvar e mostra botão cancelar
                    saveBt.visibility = View.GONE
                    cancelBt.visibility = View.VISIBLE

                    // Ação do botão cancelar: volta para MainActivity e finaliza essa
                    cancelBt.setOnClickListener {
                        val intent = Intent(this@TaskActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        // Configura botão salvar para validar e retornar a tarefa editada/criada
        with(acb) {
            saveBt.setOnClickListener {
                val title = titleEt.text.toString().trim()
                val description = descriptionEt.text.toString().trim()

                // Validação básica para campos obrigatórios
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

                // Cria ou atualiza o objeto Task
                val task = Task (
                    receivedTask?.id?:hashCode(),
                    titleEt.text.toString(),
                    descriptionEt.text.toString(),
                    deadlineEt.text.toString()
                )

                // Prepara o resultado para enviar de volta à MainActivity
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_TASK, task)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }

            // Configura botão cancelar para voltar para a MainActivity
            cancelBt.setOnClickListener {
                val intent = Intent(this@TaskActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}