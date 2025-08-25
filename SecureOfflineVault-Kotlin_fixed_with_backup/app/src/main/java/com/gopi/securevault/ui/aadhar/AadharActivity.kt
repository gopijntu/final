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

import android.content.ClipData
import android.content.ClipboardManager

class AadharActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAadharBinding
    private val dao by lazy { AppDatabase.get(this).aadharDao() }
    private val adapter = AadharAdapter(
        onEdit = { entity -> showCreateOrEditDialog(entity) },
        onDelete = { entity -> lifecycleScope.launch { dao.delete(entity) } },
        onCopy = { aadharNumber -> copyToClipboard(aadharNumber) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAadharBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

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
        val dlgBinding = com.gopi.securevault.databinding.DialogAadharBinding.inflate(layoutInflater)

        existing?.let {
            dlgBinding.etName.setText(it.name ?: "")
            dlgBinding.etNumber.setText(it.number ?: "")
            //dlgBinding.etDob.setText(it.dob ?: "")
            //dlgBinding.etAddress.setText(it.address ?: "")
        }

        val dlg = AlertDialog.Builder(this)
            .setView(dlgBinding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dlg.setOnShowListener {
            val btn = dlg.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val number = dlgBinding.etNumber.text.toString().trim()
                if (number.isBlank()) {
                    Toast.makeText(this, "Aadhar number is mandatory", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val entity = AadharEntity(
                    id = existing?.id ?: 0,
                    name = dlgBinding.etName.text.toString(),
                    number = number,
                    //dob = dlgBinding.etDob.text.toString(),
                    //address = dlgBinding.etAddress.text.toString()
                )
                lifecycleScope.launch {
                    if (existing == null) dao.insert(entity) else dao.update(entity)
                }
                dlg.dismiss()
            }
        }
        dlg.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("aadhar number", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Adapter with nested ViewHolder
 */
class AadharAdapter(
    val onEdit: (AadharEntity) -> Unit,
    val onDelete: (AadharEntity) -> Unit,
    val onCopy: (String) -> Unit
) : RecyclerView.Adapter<AadharAdapter.AadharVH>() {

    private val items = mutableListOf<AadharEntity>()

    fun submit(list: List<AadharEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AadharVH {
        val binding = ItemAadharBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AadharVH(binding, onEdit, onDelete, onCopy)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: AadharVH, position: Int) =
        holder.bind(items[position])

    // ✅ Nested ViewHolder class
    class AadharVH(
        private val binding: ItemAadharBinding,
        val onEdit: (AadharEntity) -> Unit,
        val onDelete: (AadharEntity) -> Unit,
        val onCopy: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AadharEntity) {
            binding.tvTitle.text = item.name ?: "(No Name)"
            binding.tvAadharNumber.text = item.number ?: ""
            //binding.tvDob.text = "DOB: ${item.dob ?: ""}"
           // binding.tvAddress.text = "Address: ${item.address ?: ""}"

            binding.llAadharNumber.setOnClickListener { onCopy(item.number ?: "") }
            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
