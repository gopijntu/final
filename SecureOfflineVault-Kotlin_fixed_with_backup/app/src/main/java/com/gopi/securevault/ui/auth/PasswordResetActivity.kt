package com.gopi.securevault.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gopi.securevault.databinding.ActivityPasswordResetBinding
import com.gopi.securevault.util.CryptoPrefs
import com.gopi.securevault.util.PasswordUtils
import net.sqlcipher.database.SQLiteDatabase
import java.io.File

class PasswordResetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordResetBinding
    private lateinit var prefs: CryptoPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        prefs = CryptoPrefs(this)

        // --- Safe check for security answers ---
        val a1hash = prefs.getString("a1_hash", null)
        val a2hash = prefs.getString("a2_hash", null)
        val salt1 = prefs.getString("a1_salt", null)
        val salt2 = prefs.getString("a2_salt", null)

        if (a1hash.isNullOrEmpty() || a2hash.isNullOrEmpty() ||
            salt1.isNullOrEmpty() || salt2.isNullOrEmpty()) {
            Toast.makeText(this, "Security questions not set. Cannot reset password.", Toast.LENGTH_LONG).show()
            finish() // Go back to login
            return
        }

        binding.tvQ1.text = prefs.getString("q1", "Question 1")
        binding.tvQ2.text = prefs.getString("q2", "Question 2")

        binding.btnSubmit.setOnClickListener {
            val a1 = binding.etAnswer1.text.toString().trim()
            val a2 = binding.etAnswer2.text.toString().trim()
            val n1 = binding.etNewPassword.text.toString().trim()
            val n2 = binding.etConfirmPassword.text.toString().trim()

            // Validate password fields
            if (n1 != n2) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!PasswordUtils.isPasswordValid(n1)) {
                Toast.makeText(this, "Password must be at least 8 characters with letters, numbers, and symbols.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Hash user answers with stored salts
            val h1 = PasswordUtils.hashWithSalt(a1, salt1)
            val h2 = PasswordUtils.hashWithSalt(a2, salt2)

            // Check answers
            if (h1 == a1hash && h2 == a2hash) {

                // Generate new salt and hash for new password
                val newSalt = PasswordUtils.generateSalt()
                val newHash = PasswordUtils.hashWithSalt(n1, newSalt)

                // Rekey database safely
                val dbFile = getDatabasePath("securevault.db")
                val oldHash = prefs.getString("master_hash", null)

                try {
                    if (dbFile.exists() && !oldHash.isNullOrEmpty()) {
                        val db = SQLiteDatabase.openDatabase(
                            dbFile.absolutePath,
                            oldHash.toCharArray(),
                            null,
                            SQLiteDatabase.OPEN_READWRITE
                        )
                        db.execSQL("PRAGMA rekey = '$newHash';")
                        db.close()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Warning: could not rekey DB. Restore may require old password.\nError: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Update prefs with new password hash and salt
                prefs.putString("salt", newSalt)
                prefs.putString("master_hash", newHash)

                Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show()
                finish() // go back to login
            } else {
                Toast.makeText(this, "Security answers do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
