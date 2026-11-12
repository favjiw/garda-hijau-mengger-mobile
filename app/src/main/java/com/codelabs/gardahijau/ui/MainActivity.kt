package com.codelabs.gardahijau.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.codelabs.gardahijau.BuildConfig
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityMainBinding
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.network.MainApiService
import com.codelabs.gardahijau.ui.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var mainApiService: MainApiService
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Latar terang -> ikon status bar & navigation bar gelap.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        lifecycleScope.launch {
            val token = userPreferences.getAuthToken().firstOrNull()
            if (token.isNullOrEmpty()) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
                return@launch
            }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()

        supportActionBar?.hide()
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)

        // Pastikan token masih berlaku. 401 ditangani AuthInterceptor (kembali ke halaman Masuk);
        // error lain (mis. tidak ada internet) diabaikan supaya pengguna tidak ikut keluar.
        if (!BuildConfig.USE_MOCK_API) {
            lifecycleScope.launch {
                runCatching { mainApiService.me() }
            }
        }
        }
    }

    /**
     * Konten mulai di bawah status bar. Saat keyboard terbuka (mis. di tab GardaBot), navigasi bawah
     * disembunyikan dan isi tab menyempit setinggi keyboard, supaya kotak tulis tidak tertutup.
     * Saat keyboard tertutup, BottomNavigationView mengurus inset navigation bar sendiri.
     */
    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.container) { container, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboardTerbuka = insets.isVisible(WindowInsetsCompat.Type.ime())
            val tinggiKeyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            container.updatePadding(top = bars.top, bottom = if (keyboardTerbuka) tinggiKeyboard else 0)
            binding.navView.isVisible = !keyboardTerbuka
            binding.navDivider.isVisible = !keyboardTerbuka
            insets
        }
        ViewCompat.requestApplyInsets(binding.container)
    }
}