package com.codelabs.gardahijau.ui.auth.login

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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityLoginBinding
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.ui.MainActivity
import com.codelabs.gardahijau.ui.auth.register.RegisterActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences
    private lateinit var binding : ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setTextToLogin()
        setupPasswordToggle()
        isAlreadyLoggedIn()
        setupClickListeners()
        observeViewModel()
    }

    private fun isAlreadyLoggedIn() {
        lifecycleScope.launch {
            val token = userPreferences.getAuthToken().firstOrNull()
            if (!token.isNullOrEmpty()) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
                return@launch
            }
        }
    }

    private fun observeViewModel() {
        loginViewModel.isLoading.observe(this) { isLoading ->
            binding.btnMasuk.isEnabled = !isLoading
            binding.btnMasuk.text = if (isLoading) "Loading..." else "Masuk"
        }

        loginViewModel.loginResult.observe(this) { result ->
            when (result) {
                is ApiResponse.Success -> {
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is ApiResponse.Error -> {
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is ApiResponse.Empty -> {
                    // Loading state handled above
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnMasuk.setOnClickListener {
            val username = binding.inputUsername.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (validateInput(username, password)) {
                loginViewModel.login(username, password)
            }
        }
    }

    private fun setTextToLogin() {
        val fullText = "Belum Punya Akun? Daftar"
        val target = "Daftar"
        val start = fullText.indexOf(target)
        val end = start + target.length

        val spannable = SpannableString(fullText)
        val clickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
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

    private fun validateInput(username: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.edtUsername.error = "Username tidak boleh kosong"
                false
            }
            password.isEmpty() -> {
                binding.edtPassword.error = "Password tidak boleh kosong"
                false
            }
            else -> true
        }
    }
}