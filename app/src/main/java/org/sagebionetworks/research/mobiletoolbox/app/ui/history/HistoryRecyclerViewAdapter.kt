package org.sagebionetworks.research.mobiletoolbox.app.ui.history

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.sagebionetworks.bridge.kmm.shared.repo.AssessmentHistoryRecord
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.HistoryCardBinding
import org.sagebionetworks.research.mobiletoolbox.app.ui.today.TodayFragment
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class HistoryRecyclerViewAdapter : ListAdapter<AssessmentHistoryRecord, HistoryRecyclerViewAdapter.AssessmentHistoryViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentHistoryViewHolder {
        return AssessmentHistoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AssessmentHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


    class AssessmentHistoryViewHolder(val binding: HistoryCardBinding): RecyclerView.ViewHolder(binding.root) {

        private var currentItem: AssessmentHistoryRecord? = null

        fun bind(historyItem: AssessmentHistoryRecord) {
            currentItem = historyItem
            setupCard(historyItem)
        }

        fun setupCard(historyItem: AssessmentHistoryRecord) {
            val context = binding.root.context
            val assessmentInfo = historyItem.assessmentInfo
            binding.title.text = assessmentInfo.label
            val localDateTime =
                historyItem.finishedOn.toLocalDateTime(TimeZone.currentSystemDefault())
                    .toJavaLocalDateTime()
            val text = localDateTime.format(
                DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.LONG))
            binding.date.text = text
            historyItem.minutes.let {
                binding.time.text = context.getString(R.string.number_minutes, it)
            }
            val foregroundColor = Color.parseColor(assessmentInfo.colorScheme?.foreground ?: "#000000")
            binding.imageBackground.background = ColorDrawable(foregroundColor)
            binding.check.imageTintList = ColorStateList.valueOf(foregroundColor)
            binding.check.imageTintMode = PorterDuff.Mode.MULTIPLY
            TodayFragment.assessmentIconMap.get(assessmentInfo.identifier)?.let {
                binding.assessmentImage.setImageDrawable(context.resources.getDrawable(it))
            }
        }

        companion object {
            fun from(parent: ViewGroup): AssessmentHistoryViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HistoryCardBinding.inflate(layoutInflater, parent, false)
                return AssessmentHistoryViewHolder(binding)
            }
        }
    }

}

class ItemDiffCallback : DiffUtil.ItemCallback<AssessmentHistoryRecord>() {
    override fun areItemsTheSame(oldItem: AssessmentHistoryRecord, newItem: AssessmentHistoryRecord): Boolean {
        return oldItem.instanceGuid == newItem.instanceGuid && oldItem.startedOn == newItem.startedOn
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AssessmentHistoryRecord, newItem: AssessmentHistoryRecord): Boolean {
        return oldItem == newItem
    }
}

//data class HistoryItem(
//    val assessmentHistoryRecord: AssessmentHistoryRecord,
//    //val session: ScheduledSessionWindow
//) {
//    val id: String
//        get() = assessmentHistoryRecord.instanceGuid + assessmentHistoryRecord.startedOn
//}
