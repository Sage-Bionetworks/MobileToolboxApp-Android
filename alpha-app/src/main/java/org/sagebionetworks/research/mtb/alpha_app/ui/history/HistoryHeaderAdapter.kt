package org.sagebionetworks.research.mtb.alpha_app.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.sagebionetworks.research.mtb.alpha_app.databinding.HistoryHeaderBinding


class HistoryHeaderAdapter: RecyclerView.Adapter<HistoryHeaderAdapter.HeaderViewHolder>() {

    var minutes = 0
    var loading = false

    class HeaderViewHolder(val binding: HistoryHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(minutes: Int, loading: Boolean) {
            binding.time.text = minutes.toString()
            if (loading) {
                binding.loading.visibility = View.VISIBLE
            } else {
                binding.loading.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HistoryHeaderBinding.inflate(layoutInflater, parent, false)
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(minutes, loading)
    }

    override fun getItemCount(): Int {
        return 1
    }

}
