package com.example.hotspotlogin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hotspotlogin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Set up button click listeners
        binding.userLoginButton.setOnClickListener {
            navigateToLogin(LoginActivity.LoginType.USER)
        }

        binding.voucherLoginButton.setOnClickListener {
            navigateToLogin(LoginActivity.LoginType.VOUCHER)
        }

        // Load voucher prices
        loadVoucherPrices()
    }

    private fun navigateToLogin(type: LoginActivity.LoginType) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra("LOGIN_TYPE", type.name)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun loadVoucherPrices() {
        val vouchers = listOf(
            Voucher("1 Hour", "Rp5.000"),
            Voucher("3 Hours", "Rp12.000"),
            Voucher("6 Hours", "Rp20.000"),
            Voucher("12 Hours", "Rp35.000"),
            Voucher("1 Day", "Rp50.000"),
            Voucher("3 Days", "Rp120.000"),
            Voucher("1 Week", "Rp250.000"),
            Voucher("1 Month", "Rp800.000")
        )

        val adapter = VoucherAdapter(vouchers) { voucher ->
            // Handle voucher selection
            navigateToLogin(LoginActivity.LoginType.VOUCHER)
        }
        binding.voucherRecyclerView.adapter = adapter
        binding.voucherRecyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
    }
}

data class Voucher(val duration: String, val price: String)