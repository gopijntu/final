package com.gopi.securevault.ui.home

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gopi.securevault.R
import com.gopi.securevault.databinding.ActivityHomeBinding
import com.gopi.securevault.ui.BaseActivity
import com.gopi.securevault.ui.aadhar.AadharActivity
import com.gopi.securevault.ui.auth.LoginActivity
import com.gopi.securevault.ui.banks.BanksActivity
import com.gopi.securevault.ui.cards.CardsActivity
import com.gopi.securevault.ui.license.LicenseActivity
import com.gopi.securevault.ui.pan.PanActivity
import com.gopi.securevault.ui.policies.PoliciesActivity
import com.gopi.securevault.ui.settings.SettingsActivity
import com.gopi.securevault.ui.voterid.VoterIdActivity
import com.google.android.material.card.MaterialCardView

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding

    private lateinit var menuCards: List<MaterialCardView>
    private val handler = Handler(Looper.getMainLooper())
    private var currentCardIndex = 0

    // The delay between each card's animation start
    private val cardDelayDuration = 2000L

    // The duration of the glow animation itself
    private val glowAnimationDuration = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the list of menu cards in the desired sequence
        menuCards = listOf(
            binding.btnBanks,
            binding.btnCards,
            binding.btnPolicies,
            binding.btnAadhar,
            binding.btnPan,
            binding.btnLicense,
            binding.btnVoterId,
            binding.btnSettings
        )

        // Set up click listeners for the menu items
        binding.btnBanks.setOnClickListener { startActivity(Intent(this, BanksActivity::class.java)) }
        binding.btnCards.setOnClickListener { startActivity(Intent(this, CardsActivity::class.java)) }
        binding.btnPolicies.setOnClickListener { startActivity(Intent(this, PoliciesActivity::class.java)) }
        binding.btnAadhar.setOnClickListener { startActivity(Intent(this, AadharActivity::class.java)) }
        binding.btnPan.setOnClickListener { startActivity(Intent(this, PanActivity::class.java)) }
        binding.btnVoterId.setOnClickListener { startActivity(Intent(this, VoterIdActivity::class.java)) }
        binding.btnLicense.setOnClickListener { startActivity(Intent(this, LicenseActivity::class.java)) }
        binding.btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.btnLogout.setOnClickListener {
            // TODO: Implement a proper logout mechanism
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        // Start the sequential glow animation
        startGlowAnimation()
    }

    private fun startGlowAnimation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (menuCards.isEmpty()) return

                // Animate the stroke off for the previous card
                val previousCardIndex = (currentCardIndex - 1 + menuCards.size) % menuCards.size
                animateStroke(menuCards[previousCardIndex], false)

                // Animate the stroke on for the current card
                val currentCard = menuCards[currentCardIndex]
                animateStroke(currentCard, true)

                // Move to the next card in the list
                currentCardIndex = (currentCardIndex + 1) % menuCards.size

                // Schedule the next animation
                handler.postDelayed(this, cardDelayDuration)
            }
        }, cardDelayDuration)
    }

    private fun animateStroke(card: MaterialCardView, isGlowing: Boolean) {
        val startWidth = if (isGlowing) 0f else 4f
        val endWidth = if (isGlowing) 4f else 0f

        val startColor = if (isGlowing) ContextCompat.getColor(this, R.color.transparent_neon) else ContextCompat.getColor(this, R.color.glow_cyan)
        val endColor = if (isGlowing) ContextCompat.getColor(this, R.color.glow_cyan) else ContextCompat.getColor(this, R.color.transparent_neon)

        // Animate the stroke width
        ObjectAnimator.ofFloat(card, "strokeWidth", startWidth, endWidth).apply {
            duration = glowAnimationDuration
            start()
        }

        // Animate the stroke color
        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        colorAnimator.duration = glowAnimationDuration
        colorAnimator.addUpdateListener { animator ->
            card.strokeColor = animator.animatedValue as Int
        }
        colorAnimator.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the animation to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
    }
}
