package org.sagebionetworks.research.mobiletoolbox.app.ui.today

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.TodayHeaderBinding
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class HeaderAdapter: RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {

    var todayComplete = false
    var loading = false
    var resultsUploading = false
    var hasInternet = true

    private fun getUploadText(context: Context) : String {
        if (!hasInternet) {
            return context.getString(R.string.please_connect_to_internet)
        } else if (todayComplete) {
            return context.getString(R.string.your_results_are_uploading_wait)
        } else {
            return context.getString(R.string.your_results_are_uploading)
        }
    }

    /* ViewHolder for displaying today header. */
    class HeaderViewHolder(val binding: TodayHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String, upToDate: Boolean, loading: Boolean, resultsUploading: Boolean, hasInternet: Boolean, uploadText: String) {

            val showUpToDate = !loading && upToDate && !resultsUploading
            val showActivities = !loading && !upToDate
            val showUploadStatus = !loading && resultsUploading

            binding.todaysDate.text = text
            binding.loading.visibility = if (loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (showUploadStatus) {
                binding.uploadStatusView.visibility = View.VISIBLE
                binding.uploadText.text = uploadText
                if (hasInternet) {
                    binding.uploadProgress.visibility = View.VISIBLE
                } else {
                    binding.uploadProgress.visibility = View.GONE
                }
            } else {
                binding.uploadStatusView.visibility = View.GONE
            }
            binding.upToDate.visibility = if (showUpToDate) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.currentActivities.visibility = if (showActivities) {
                View.VISIBLE
            } else {
                View.GONE
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
        holder.bind(text, todayComplete, loading, resultsUploading, hasInternet, getUploadText(holder.binding.uploadStatusView.context))
    }

    override fun getItemCount(): Int {
        return 1
    }

}
