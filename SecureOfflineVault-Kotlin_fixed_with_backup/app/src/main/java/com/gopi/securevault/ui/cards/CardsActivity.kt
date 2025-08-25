package com.gopi.securevault.ui.cards

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
import com.gopi.securevault.data.entities.CardEntity
import com.gopi.securevault.databinding.ActivityCardsBinding
import com.gopi.securevault.databinding.ItemCardBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CardsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCardsBinding
    private val dao by lazy { AppDatabase.get(this).cardDao() }
    private val adapter = CardAdapter(
        onEdit = { entity -> showCreateOrEditDialog(entity) },
        onDelete = { entity -> lifecycleScope.launch { dao.delete(entity) } }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.fabAdd.setOnClickListener { showCreateOrEditDialog(null) }

        lifecycleScope.launch {
            dao.observeAll().collectLatest { list -> adapter.submit(list) }
        }
    }

    private fun showCreateOrEditDialog(existing: CardEntity?) {
        val dlgView = layoutInflater.inflate(com.gopi.securevault.R.layout.dialog_card, null)
        val etBank = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etBankName)
        val etNumber = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etCardNumber)
        val etType = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etCardType)
        val etValid = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etValidTill)
        val etCvv = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etCVV)
        val etNote = dlgView.findViewById<android.widget.EditText>(com.gopi.securevault.R.id.etNote)

        existing?.let {
            etBank.setText(it.bankName ?: "")
            etNumber.setText(it.cardNumber ?: "")
            etType.setText(it.cardType ?: "")
            etValid.setText(it.validTill ?: "")
            etCvv.setText(it.cvv ?: "")
            etNote.setText(it.note ?: "")
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
                    Toast.makeText(this, "Card number is mandatory", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val entity = CardEntity(
                    id = existing?.id ?: 0,
                    bankName = etBank.text.toString(),
                    cardType = etType.text.toString(),
                    cardNumber = number,
                    cvv = etCvv.text.toString(),
                    validTill = etValid.text.toString(),
                    note = etNote.text.toString()
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

class CardAdapter(
    val onEdit: (CardEntity) -> Unit,
    val onDelete: (CardEntity) -> Unit
) : RecyclerView.Adapter<CardVH>() {
    private val items = mutableListOf<CardEntity>()

    fun submit(list: List<CardEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardVH {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardVH(binding, onEdit, onDelete)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: CardVH, position: Int) =
        holder.bind(items[position])
}

class CardVH(
    private val binding: ItemCardBinding,
    val onEdit: (CardEntity) -> Unit,
    val onDelete: (CardEntity) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CardEntity) {
        // Title and core fields
        binding.tvTitle.text = item.bankName ?: "(No Bank)"
        binding.tvCardNumber.text = "Card No: ${item.cardNumber ?: ""}"
        binding.tvExpiry.text = "Expiry: ${item.validTill ?: ""}"

        // Mask CVV
        binding.tvCvv.text = "CVV: ${item.cvv ?: ""}"

        // Show bank name again if you want as subtitle
        binding.tvBankName.text = "Bank: ${item.bankName ?: ""}"

        // Card type / Note if available
        binding.tvCardHolder.text = item.cardType ?: ""

        // Buttons
        binding.btnEdit.setOnClickListener { onEdit(item) }
        binding.btnDelete.setOnClickListener { onDelete(item) }
    }
}
