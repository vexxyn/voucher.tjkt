package com.example.hotspotlogin

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.hotspotlogin.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var loginType: LoginType = LoginType.USER
    private var networkTimer: CountDownTimer? = null
    private var voucherTimer: CountDownTimer? = null

    enum class LoginType { USER, VOUCHER }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginType = intent.getStringExtra("LOGIN_TYPE")?.let {
            LoginType.valueOf(it)
        } ?: LoginType.USER

        setupUI()
    }

    private fun setupUI() {
        when (loginType) {
            LoginType.USER -> setupUserLogin()
            LoginType.VOUCHER -> setupVoucherLogin()
        }

        binding.backButton.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun setupUserLogin() {
        binding.title.text = getString(R.string.user_login)
        binding.usernameContainer.visibility = View.VISIBLE
        binding.passwordContainer.visibility = View.VISIBLE
        binding.voucherCodeContainer.visibility = View.GONE
        binding.networkStatusContainer.visibility = View.VISIBLE
        binding.timeRemainingContainer.visibility = View.GONE

        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                showError(getString(R.string.fill_all_fields))
                return@setOnClickListener
            }

            authenticateUser(username, password)
        }

        startNetworkMonitoring()
    }

    private fun setupVoucherLogin() {
        binding.title.text = getString(R.string.voucher_login)
        binding.usernameContainer.visibility = View.GONE
        binding.passwordContainer.visibility = View.GONE
        binding.voucherCodeContainer.visibility = View.VISIBLE
        binding.networkStatusContainer.visibility = View.GONE
        binding.timeRemainingContainer.visibility = View.VISIBLE

        binding.loginButton.setOnClickListener {
            val voucherCode = binding.voucherCodeEditText.text.toString()

            if (voucherCode.isEmpty()) {
                showError(getString(R.string.enter_voucher_code))
                return@setOnClickListener
            }

            validateVoucher(voucherCode)
        }
    }

    private fun authenticateUser(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        // Simulate network call
        android.os.Handler().postDelayed({
            val isSuccess = listOf(true, true, true, false).random() // 75% success rate for demo
            
            if (isSuccess) {
                showSuccess(getString(R.string.login_success))
                // Navigate to success screen
            } else {
                showError(getString(R.string.invalid_credentials))
                binding.loginButton.isEnabled = true
            }
            
            binding.progressBar.visibility = View.GONE
        }, 1500)
    }

    private fun validateVoucher(code: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        // Simulate network call
        android.os.Handler().postDelayed({
            val isSuccess = listOf(true, true, true, false).random() // 75% success rate for demo
            val duration = 3600 // 1 hour in seconds
            
            if (isSuccess) {
                showSuccess(getString(R.string.voucher_accepted))
                startVoucherTimer(duration)
                // Navigate to success screen
            } else {
                showError(getString(R.string.invalid_voucher))
                binding.loginButton.isEnabled = true
            }
            
            binding.progressBar.visibility = View.GONE
        }, 1500)
    }

    private fun startNetworkMonitoring() {
        networkTimer?.cancel()
        
        networkTimer = object : CountDownTimer(Long.MAX_VALUE, 2000) {
            override fun onTick(millisUntilFinished: Long) {
                val speed = (5..100).random()
                binding.speedValue.text = getString(R.string.speed_format, speed)

                val usage = (100..500).random()
                binding.dataUsageValue.text = getString(R.string.usage_format, usage)
            }

            override fun onFinish() {}
        }.start()
    }

    private fun startVoucherTimer(duration: Int) {
        voucherTimer?.cancel()
        
        var remaining = duration * 1000L // Convert to milliseconds
        
        voucherTimer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remaining = millisUntilFinished
                
                val hours = (millisUntilFinished / 3600000).toInt()
                val minutes = ((millisUntilFinished % 3600000) / 60000).toInt()
                val seconds = ((millisUntilFinished % 60000) / 1000).toInt()
                
                binding.timeRemainingValue.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                binding.timeRemainingValue.text = "00:00:00"
                showWarning(getString(R.string.voucher_expired))
            }
        }.start()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.error))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    private fun showWarning(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.warning))
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
    }

    override fun onDestroy() {
        networkTimer?.cancel()
        voucherTimer?.cancel()
        super.onDestroy()
    }
}