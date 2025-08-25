package com.gopi.securevault.ui.aadhar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gopi.securevault.data.db.AppDatabase
import com.gopi.securevault.data.entities.AadharEntity
import com.gopi.securevault.databinding.ActivityAadharBinding
import com.gopi.securevault.databinding.ItemAadharBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AadharActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAadharBinding
    private val dao by lazy { AppDatabase.get(this).aadharDao() }
    private val adapter = AadharAdapter(
        onEdit = { entity -> showCreateOrEditDialog(entity) },
        onDelete = { entity -> lifecycleScope.launch { dao.delete(entity) } }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAadharBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        // Add new entry
        binding.fabAdd.setOnClickListener { showCreateOrEditDialog(null) }

        // Observe DB
        lifecycleScope.launch {
            dao.observeAll().collectLatest { list -> adapter.submit(list) }
        }
    }

    private fun showCreateOrEditDialog(existing: AadharEntity?) {
        val dlgView = layoutInflater.inflate(com.gopi.securevault.R.layout.dialog_aadhar, null)

        val etName = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etName)
        val etNumber = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etNumber)
        val etDob = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etDob)
        val etAddress = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etAddress)

        existing?.let {
            etName.setText(it.name ?: "")
            etNumber.setText(it.number ?: "")
            //etDob.setText(it.dob ?: "")
            //etAddress.setText(it.address ?: "")
        }

        val dlg = AlertDialog.Builder(this)
            .setView(dlgView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dlg.setOnShowListener {
            val btn = dlg.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val number = etNumber.text.toString().trim()
                if (number.isBlank()) {
                    Toast.makeText(this, "Aadhar number is mandatory", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val entity = AadharEntity(
                    id = existing?.id ?: 0,
                    name = etName.text.toString(),
                    number = number,
                    //dob = etDob.text.toString(),
                    //address = etAddress.text.toString()
                )
                lifecycleScope.launch {
                    if (existing == null) dao.insert(entity) else dao.update(entity)
                }
                dlg.dismiss()
            }
        }
        dlg.show()
    }
}

/**
 * Adapter with nested ViewHolder
 */
class AadharAdapter(
    val onEdit: (AadharEntity) -> Unit,
    val onDelete: (AadharEntity) -> Unit
) : RecyclerView.Adapter<AadharAdapter.AadharVH>() {

    private val items = mutableListOf<AadharEntity>()

    fun submit(list: List<AadharEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AadharVH {
        val binding = ItemAadharBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AadharVH(binding, onEdit, onDelete)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AadharVH, position: Int) =
        holder.bind(items[position])

    // ✅ Nested ViewHolder class
    class AadharVH(
        private val binding: ItemAadharBinding,
        val onEdit: (AadharEntity) -> Unit,
        val onDelete: (AadharEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AadharEntity) {
            binding.tvTitle.text = item.name ?: "(No Name)"
            binding.tvAadharNumber.text = "Aadhar: ${item.number ?: ""}"
            //binding.tvDob.text = "DOB: ${item.dob ?: ""}"
           // binding.tvAddress.text = "Address: ${item.address ?: ""}"

            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
