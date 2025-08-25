package com.gopi.securevault.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gopi.securevault.databinding.ActivitySetupBinding
import com.gopi.securevault.ui.home.HomeActivity
import com.gopi.securevault.util.CryptoPrefs
import com.gopi.securevault.util.PasswordUtils

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding
    private lateinit var prefs: CryptoPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        prefs = CryptoPrefs(this)

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val pwd = binding.etPassword.text.toString()
            val q1 = binding.etQ1.text.toString().trim()
            val a1 = binding.etA1.text.toString()
            val q2 = binding.etQ2.text.toString().trim()
            val a2 = binding.etA2.text.toString()

            if (name.isEmpty() || pwd.length < 6 || q1.isEmpty() || a1.isEmpty() || q2.isEmpty() || a2.isEmpty()) {
                Toast.makeText(this, "Fill all fields (password min 6 chars)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val salt = PasswordUtils.generateSalt()
            val hash = PasswordUtils.hashWithSalt(pwd, salt)

            val asalt1 = PasswordUtils.generateSalt()
            val ahash1 = PasswordUtils.hashWithSalt(a1, asalt1)
            val asalt2 = PasswordUtils.generateSalt()
            val ahash2 = PasswordUtils.hashWithSalt(a2, asalt2)

            prefs.putString("user_name", name)
            prefs.putString("salt", salt)
            prefs.putString("master_hash", hash)

            prefs.putString("q1", q1)
            prefs.putString("a1_salt", asalt1)
            prefs.putString("a1_hash", ahash1)
            prefs.putString("q2", q2)
            prefs.putString("a2_salt", asalt2)
            prefs.putString("a2_hash", ahash2)

            prefs.putBoolean("is_setup_done", true)

            Toast.makeText(this, "Setup complete!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}
