package com.codelabs.gardahijau.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityNotificationsBinding
import com.codelabs.gardahijau.model.remote.response.notification.NotificationItem
import com.codelabs.gardahijau.ui.MainActivity
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val viewModel: NotificationViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupToolbar()
        setupRecycler()
        setupFilterButtons()
        setupObservers()

        viewModel.getNotification()  // fetch API
    }


    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }
    }

    private fun setupRecycler() {
        notificationAdapter = NotificationAdapter(emptyList()) { item ->
            viewModel.markNotificationAsRead(item)

            val intent = Intent(this, NotificationDetailActivity::class.java)
            intent.putExtra("notif_data", item)
            startActivity(intent)
        }
        binding.recyclerNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
        }
    }

    private fun setupObservers() {

        viewModel.isLoading.observe(this) { loading ->
            //binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // observe data yang sudah difilter
        viewModel.filteredData.observe(this) { list ->
            notificationAdapter.updateData(list)
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                //binding.textError.visibility = View.VISIBLE
                //binding.textError.text = msg
            }
        }
    }


    private fun setupFilterButtons() {

        binding.btnSemua.setOnClickListener {
            viewModel.setFilter(NotificationViewModel.FilterType.SEMUA)
            setButtonActive(binding.btnSemua)
        }

        binding.btnBelum.setOnClickListener {
            viewModel.setFilter(NotificationViewModel.FilterType.BELUM)
            setButtonActive(binding.btnBelum)
        }

        binding.btnDibaca.setOnClickListener {
            viewModel.setFilter(NotificationViewModel.FilterType.DIBACA)
            setButtonActive(binding.btnDibaca)
        }
    }

    private fun setButtonActive(active: Button) {
        val buttons = listOf(binding.btnSemua, binding.btnBelum, binding.btnDibaca)

        buttons.forEach { button ->
            if (button == active) {
                button.setBackgroundTintList(getColorStateList(R.color.green))
                button.setTextColor(getColor(R.color.white))
            } else {
                button.setBackgroundTintList(getColorStateList(R.color.gray))
                button.setTextColor(getColor(R.color.gray_light))
            }
        }
    }



    // ADAPTER
    inner class NotificationAdapter(
        private var items: List<NotificationItem>,
        private val onItemClicked: (NotificationItem) -> Unit
    ) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

        inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: TextView = itemView.findViewById(R.id.notificationIcon)
            val title: TextView = itemView.findViewById(R.id.notificationTitle)
            val description: TextView = itemView.findViewById(R.id.notificationDescription)
            val time: TextView = itemView.findViewById(R.id.notificationTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return NotificationViewHolder(view)
        }

        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            val item = items[position]

            holder.icon.text = "🔔"
            holder.title.text = item.title
            holder.description.text = item.message
            holder.time.text = item.createdAt.replace("T", " ").substring(0, 16)

            // === KLIK ITEM ===
            holder.itemView.setOnClickListener {
                onItemClicked(item)
            }
        }

        override fun getItemCount() = items.size

        fun updateData(newItems: List<NotificationItem>) {
            items = newItems
            notifyDataSetChanged()
        }
    }
}
