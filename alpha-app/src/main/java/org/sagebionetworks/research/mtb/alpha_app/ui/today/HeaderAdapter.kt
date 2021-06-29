package org.sagebionetworks.research.mtb.alpha_app.ui.today

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.sagebionetworks.research.mtb.alpha_app.databinding.TodayHeaderBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class HeaderAdapter: RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {

    /* ViewHolder for displaying header. */
    class HeaderViewHolder(val binding: TodayHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String) {
            binding.todaysDate.text = text
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
        holder.bind(text)
    }

    override fun getItemCount(): Int {
        return 1
    }

}
