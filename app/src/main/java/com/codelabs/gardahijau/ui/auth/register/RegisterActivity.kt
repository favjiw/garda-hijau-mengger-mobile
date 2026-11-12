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
import android.text.style.StyleSpan
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityRegisterBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.ui.auth.login.LoginActivity
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
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
        // Kepala hijau di belakang status bar -> ikon status bar putih.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutHero.applySystemBarPadding(top = true)
        binding.layoutFooter.applySystemBarPadding(bottom = true, includeIme = true)

        setupRwChoice()
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
                binding.edtPassword.endIconContentDescription = getString(R.string.ghm_sembunyikan_sandi)
            } else {
                binding.inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.edtPassword.setEndIconDrawable(R.drawable.ic_eye_visibility_off)
                binding.edtPassword.endIconContentDescription = getString(R.string.ghm_tampilkan_sandi)
            }
            binding.inputPassword.setSelection(binding.inputPassword.text?.length ?: 0)
        }
    }

    /** Dua tombol pilihan RW (Figma: "Kamu mengelola RW berapa?"). Hanya satu yang aktif. */
    private fun setupRwChoice() {
        val options: Map<MaterialButton, RwNumber> = mapOf(
            binding.btnRw4 to RwNumber.RW_4,
            binding.btnRw5 to RwNumber.RW_5,
        )
        options.forEach { (button, rw) ->
            button.setOnClickListener {
                options.keys.forEach { it.isChecked = it == button }
                selectedRwValue = rw.value
                binding.tvRwError.visibility = View.GONE
            }
        }
    }

    private fun observeViewModel() {
        loginViewModel.isLoading.observe(this) { isLoading ->
            binding.btnDaftar.isEnabled = !isLoading
            binding.btnDaftar.text = getString(if (isLoading) R.string.ghm_memproses else R.string.ghm_tombol_daftar)
        }

        loginViewModel.registerResult.observe(this) { result ->
            when (result) {
                is ApiResponse.Success -> {
                    Toast.makeText(this, R.string.ghm_daftar_berhasil, Toast.LENGTH_SHORT).show()
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
            val rwValue = selectedRwValue

            if (validateInput(username, password, rwValue)) {
                loginViewModel.register(username, password, rwValue)
            }
        }
    }

    private fun setTextToLogin() {
        val fullText = getString(R.string.ghm_sudah_punya_akun)
        val target = getString(R.string.ghm_tautan_masuk)
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
                ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.ghm_daun_700)
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

    private fun validateInput(username: String, password: String, rw : String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.edtUsername.error = getString(R.string.ghm_galat_nama_kosong)
                false
            }
            password.isEmpty() -> {
                binding.edtPassword.error = getString(R.string.ghm_galat_sandi_kosong)
                false
            }
            rw.isEmpty() -> {
                binding.tvRwError.visibility = View.VISIBLE
                false
            }
            else -> true
        }
    }
}
