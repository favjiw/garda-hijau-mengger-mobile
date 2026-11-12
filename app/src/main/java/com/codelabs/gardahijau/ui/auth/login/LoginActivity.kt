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
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.codelabs.gardahijau.BuildConfig
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityLoginBinding
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.ui.MainActivity
import com.codelabs.gardahijau.ui.auth.register.RegisterActivity
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
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
        // Kepala hijau di belakang status bar -> ikon status bar putih.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutHero.applySystemBarPadding(top = true)
        // Padding bawah di root: layar menyempit saat keyboard muncul, lalu isian yang aktif digulir ke atas keyboard.
        binding.main.applySystemBarPadding(bottom = true, includeIme = true)
        binding.tvModeDemo.visibility = if (BuildConfig.USE_MOCK_API) View.VISIBLE else View.GONE

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
            binding.btnMasuk.text = getString(if (isLoading) R.string.ghm_memproses else R.string.ghm_tombol_masuk)
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
        val fullText = getString(R.string.ghm_belum_punya_akun)
        val target = getString(R.string.ghm_tautan_daftar)
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
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.ghm_daun_700)
                ds.isUnderlineText = false
            }
        }

        if (start >= 0) {
            spannable.setSpan(clickable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                binding.edtPassword.endIconContentDescription = getString(R.string.ghm_sembunyikan_sandi)
            } else {
                binding.inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.edtPassword.setEndIconDrawable(R.drawable.ic_eye_visibility_off)
                binding.edtPassword.endIconContentDescription = getString(R.string.ghm_tampilkan_sandi)
            }
            binding.inputPassword.setSelection(binding.inputPassword.text?.length ?: 0)
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.edtUsername.error = getString(R.string.ghm_galat_nama_kosong)
                false
            }
            password.isEmpty() -> {
                binding.edtPassword.error = getString(R.string.ghm_galat_sandi_kosong)
                false
            }
            else -> true
        }
    }
}