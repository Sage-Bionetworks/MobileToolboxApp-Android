package org.sagebionetworks.research.mtb.alpha_app.ui.history

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledAssessmentReference
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionWindow
import org.sagebionetworks.research.mtb.alpha_app.R
import org.sagebionetworks.research.mtb.alpha_app.databinding.HistoryCardBinding
import org.sagebionetworks.research.mtb.alpha_app.ui.today.TodayFragment
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class HistoryRecyclerViewAdapter() : ListAdapter<AssessmentItem, HistoryRecyclerViewAdapter.AssessmentHistoryViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentHistoryViewHolder {
        return AssessmentHistoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AssessmentHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


    class AssessmentHistoryViewHolder(val binding: HistoryCardBinding): RecyclerView.ViewHolder(binding.root) {

        private var currentItem: AssessmentItem? = null

        fun bind(assessmentItem: AssessmentItem) {
            currentItem = assessmentItem
            setupCard(assessmentItem.assessmentRef)
        }

        fun setupCard(assessmentRef: ScheduledAssessmentReference) {
            val context = binding.root.context
            val assessmentInfo = assessmentRef.assessmentInfo
            binding.title.text = assessmentInfo.label
            val adherenceRecord = assessmentRef.adherenceRecordList?.first { it.finishedOn != null }
            val localDateTime = adherenceRecord?.finishedOn?.toLocalDateTime(TimeZone.currentSystemDefault())?.toJavaLocalDateTime()
            val text = localDateTime?.format(
                DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.LONG))
            binding.date.text = text
            assessmentInfo.minutesToComplete?.let {
                binding.time.text = context.getString(R.string.number_minutes, it)
            }
            val foregroundColor = Color.parseColor(assessmentInfo.colorScheme?.foreground)
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

class ItemDiffCallback : DiffUtil.ItemCallback<AssessmentItem>() {
    override fun areItemsTheSame(oldItem: AssessmentItem, newItem: AssessmentItem): Boolean {
        return oldItem.id == newItem.id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AssessmentItem, newItem: AssessmentItem): Boolean {
        return oldItem == newItem
    }
}

data class AssessmentItem(
    val assessmentRef: ScheduledAssessmentReference,
    val session: ScheduledSessionWindow
) {
    val id: String
        get() = assessmentRef.instanceGuid
}
