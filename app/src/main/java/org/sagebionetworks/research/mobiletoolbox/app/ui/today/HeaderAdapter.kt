package org.sagebionetworks.research.mobiletoolbox.app.ui.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.sagebionetworks.research.mobiletoolbox.app.databinding.TodayHeaderBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class HeaderAdapter: RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {

    var todayComplete = false
    var loading = false

    /* ViewHolder for displaying today header. */
    class HeaderViewHolder(val binding: TodayHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String, upToDate: Boolean, loading: Boolean) {
            binding.todaysDate.text = text
            if (loading) {
                binding.loading.visibility = View.VISIBLE
                binding.currentActivities.visibility = View.GONE
                binding.upToDate.visibility = View.GONE
            } else if (upToDate) {
                binding.loading.visibility = View.GONE
                binding.currentActivities.visibility = View.GONE
                binding.upToDate.visibility = View.VISIBLE
            } else {
                binding.loading.visibility = View.GONE
                binding.currentActivities.visibility = View.VISIBLE
                binding.upToDate.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TodayHeaderBinding.inflate(layoutInflater, parent, false)
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val text = java.time.LocalDate.now().format(
            DateTimeFormatter.ofLocalizedDate(
                FormatStyle.LONG))
        holder.bind(text, todayComplete, loading)
    }

    override fun getItemCount(): Int {
        return 1
    }

}
