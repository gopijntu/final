package com.gopi.securevault.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gopi.securevault.R
import net.sqlcipher.database.SQLiteDatabase
import java.io.File

import com.gopi.securevault.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnBackup.setOnClickListener {
            showPasswordDialog { password ->
                val backupFile = File(getExternalFilesDir(null), "securevault_backup.db")
                backupDatabase(this, password, backupFile)
            }
        }

        binding.btnRestore.setOnClickListener {
            showPasswordDialog { password ->
                val backupFile = File(getExternalFilesDir(null), "securevault_backup.db")
                if (backupFile.exists()) {
                    restoreDatabase(this, password, backupFile)
                } else {
                    Toast.makeText(this, "No backup file found!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnChangePassword.setOnClickListener {
            Toast.makeText(this, "Change password feature not yet implemented.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPasswordDialog(onPasswordEntered: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_password_prompt, null)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)

        AlertDialog.Builder(this)
            .setTitle("Enter Master Password")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val password = etPassword.text.toString()
                if (password.isNotEmpty()) {
                    onPasswordEntered(password)
                } else {
                    Toast.makeText(this, "Password required!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun backupDatabase(context: Context, password: String, backupFile: File) {
        try {
            val dbFile = context.getDatabasePath("securevault.db")

            val db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                password,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )

            // Ensure old backup is removed
            if (backupFile.exists()) backupFile.delete()

            db.rawExecSQL("ATTACH DATABASE '${backupFile.absolutePath}' AS backup KEY '$password';")
            db.rawExecSQL("SELECT sqlcipher_export('backup');")
            db.rawExecSQL("DETACH DATABASE backup;")

            db.close()
            Toast.makeText(context, "Backup successful: ${backupFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun restoreDatabase(context: Context, password: String, backupFile: File) {
        try {
            val dbFile = context.getDatabasePath("securevault.db")
            if (dbFile.exists()) dbFile.delete()

            val db = SQLiteDatabase.openOrCreateDatabase(
                dbFile.absolutePath,
                password,
                null
            )

            db.rawExecSQL("ATTACH DATABASE '${backupFile.absolutePath}' AS backup KEY '$password';")
            db.rawExecSQL("SELECT sqlcipher_export('main', 'backup');")
            db.rawExecSQL("DETACH DATABASE backup;")

            db.close()
            Toast.makeText(context, "Restore successful! Restart app.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
