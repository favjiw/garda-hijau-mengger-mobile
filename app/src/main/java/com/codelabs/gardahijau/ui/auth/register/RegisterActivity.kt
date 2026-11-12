package com.codelabs.gardahijau.ui.auth.register

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityRegisterBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.ui.auth.login.LoginActivity
import com.codelabs.gardahijau.utils.enumClass.RwNumber
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val loginViewModel: RegisterViewModel by viewModels()
    private var selectedRwValue: String = ""
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupRwDropdown()
        setupPasswordToggle()
        setTextToLogin()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupPasswordToggle() {
        binding.inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.edtPassword.setEndIconOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.inputPassword.transformationMethod = null
                binding.edtPassword.setEndIconDrawable(R.drawable.ic_eye_visibility)
                binding.edtPassword.endIconContentDescription = "Hide password"
            } else {
                binding.inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.edtPassword.setEndIconDrawable(R.drawable.ic_eye_visibility_off)
                binding.edtPassword.endIconContentDescription = "Show password"
            }
            binding.inputPassword.setSelection(binding.inputPassword.text?.length ?: 0)
        }
    }

    private fun setupRwDropdown() {
        val rwField = binding.inputNomorRW as AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, RwNumber.labels())
        rwField.setAdapter(adapter)

        rwField.setOnItemClickListener { _, _, position, _ ->
            val label = adapter.getItem(position) ?: ""
            selectedRwValue = RwNumber.fromLabel(label)?.value ?: RwNumber.fromInput(label)?.value.orEmpty()
        }
    }

    private fun observeViewModel() {
        loginViewModel.isLoading.observe(this) { isLoading ->
            binding.btnDaftar.isEnabled = !isLoading
            binding.btnDaftar.text = if (isLoading) "Loading..." else "Daftar"
        }

        loginViewModel.registerResult.observe(this) { result ->
            when (result) {
                is ApiResponse.Success -> {
                    Toast.makeText(this, "Daftar berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is ApiResponse.Error -> {
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is ApiResponse.Empty -> { }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnDaftar.setOnClickListener {
            val username = binding.inputUsername.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val rwInput = binding.inputNomorRW.text.toString().trim()

            val rwValue = when {
                selectedRwValue.isNotEmpty() -> selectedRwValue
                RwNumber.fromInput(rwInput) != null -> RwNumber.fromInput(rwInput)!!.value
                else -> {
                    val digits = rwInput.replace(Regex("\\D+"), "")
                    if (digits.isNotEmpty()) {
                        val num = digits.toIntOrNull() ?: 0
                        if (num in 1..9) "RW0$num" else "RW$num"
                    } else {
                        ""
                    }
                }
            }

            if (validateInput(username, password, rwValue)) {
                loginViewModel.register(username, password, rwValue)
            }
        }
    }

    private fun setTextToLogin() {
        val fullText = "Sudah Punya Akun? Masuk"
        val target = "Masuk"
        val start = fullText.indexOf(target)
        val end = start + target.length

        val spannable = SpannableString(fullText)
        val clickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#1E88E5")
                ds.isUnderlineText = false
            }
        }

        if (start >= 0) {
            spannable.setSpan(clickable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.tvAlreadyAccount.text = spannable
            binding.tvAlreadyAccount.movementMethod = LinkMovementMethod.getInstance()
            binding.tvAlreadyAccount.highlightColor = Color.TRANSPARENT
        }
    }

    private fun validateInput(username: String, password: String, rw : String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.edtUsername.error = "Username tidak boleh kosong"
                false
            }
            password.isEmpty() -> {
                binding.edtPassword.error = "Password tidak boleh kosong"
                false
            }
            rw.isEmpty() -> {
                binding.edtRukunWarga.error = "RW tidak boleh kosong"
                false
            }
            else -> true
        }
    }
}
