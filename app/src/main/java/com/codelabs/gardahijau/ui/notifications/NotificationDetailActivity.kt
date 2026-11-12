package com.codelabs.gardahijau.ui.notifications

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityNotificationDetailBinding
import com.codelabs.gardahijau.model.remote.response.notification.NotificationItem

class NotificationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        val notif = intent.getParcelableExtra<NotificationItem>("notif_data")
        if (notif != null) {
            setupUI(notif)
        }
    }

    private fun setupUI(notif: NotificationItem) {
        binding.tvTitle.text = notif.title
        binding.tvMessage.text = notif.message
        binding.tvDate.text = notif.createdAt.replace("T", " ").substring(0, 16)
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Detail Notifikasi"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
