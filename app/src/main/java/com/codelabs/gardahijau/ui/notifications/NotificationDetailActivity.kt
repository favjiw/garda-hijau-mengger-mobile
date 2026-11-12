package com.codelabs.gardahijau.ui.notifications

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.databinding.ActivityNotificationDetailBinding
import com.codelabs.gardahijau.model.remote.response.notification.NotificationItem
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import com.codelabs.gardahijau.ui.util.parseServerDate
import java.text.SimpleDateFormat

/** Detail satu notifikasi. Data datang lewat extra "notif_data" dari NotificationsActivity. */
class NotificationDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityNotificationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutBilahAtas.applySystemBarPadding(top = true)
        binding.scrollIsi.applySystemBarPadding(bottom = true)

        binding.btnKembali.setOnClickListener { finish() }

        val notif = intent.getParcelableExtra<NotificationItem>("notif_data")
        if (notif != null) {
            setupUI(notif)
        }
    }

    private fun setupUI(notif: NotificationItem) {
        binding.tvTitle.text = notif.title
        binding.tvMessage.text = notif.message
        // Tanggal dari server dalam UTC; tampilkan dalam zona waktu HP.
        binding.tvDate.text = parseServerDate(notif.createdAt)
            ?.let { SimpleDateFormat("d MMMM yyyy, HH.mm", LOCALE_ID).format(it) }
            ?: notif.createdAt
    }
}
