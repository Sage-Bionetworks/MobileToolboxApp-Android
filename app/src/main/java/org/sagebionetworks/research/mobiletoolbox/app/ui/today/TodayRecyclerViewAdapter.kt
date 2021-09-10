package org.sagebionetworks.research.mobiletoolbox.app.ui.today

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import kotlinx.datetime.toJavaLocalDateTime
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledAssessmentReference
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionWindow
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.AssessmentCardBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.DueDateHeaderBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class TodayRecyclerViewAdapter(private val onClick: (ScheduledAssessmentReference, ScheduledSessionWindow) -> Unit) : ListAdapter<DataItem, RecyclerView.ViewHolder>(ItemDiffCallback()) {


    companion object {
        const val VIEW_TYPE_SESSION_HEADER = 1
        const val VIEW_TYPE_ASSESSMENT = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SESSION_HEADER -> SessionHeaderViewHolder.from(parent)
            VIEW_TYPE_ASSESSMENT -> AssessmentViewHolder.from(parent, onClick)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is AssessmentViewHolder -> {
                holder.bind(item as AssessmentItem)
            }
            is SessionHeaderViewHolder -> {
                val sessionHeaderItem = item as SessionHeaderItem
                holder.setupHeader(sessionHeaderItem.scheduledSessionWindow)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SessionHeaderItem -> VIEW_TYPE_SESSION_HEADER
            is AssessmentItem -> VIEW_TYPE_ASSESSMENT
        }
    }

    class SessionHeaderViewHolder(val binding: DueDateHeaderBinding): RecyclerView.ViewHolder(binding.root) {

        fun setupHeader(session: ScheduledSessionWindow) {
            //If available now
            if (session.isAvailableNow()) {
                binding.dueTextView.visibility = View.VISIBLE
                binding.opensTextView.visibility = View.GONE
                val expirationString = session.endDateTime.toJavaLocalDateTime().format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .withZone(ZoneId.systemDefault())
                )
                binding.dueTextView.text =
                    binding.dueTextView.context.getString(R.string.due_on, expirationString)
            } else {
                //If available in future
                binding.dueTextView.visibility = View.GONE
                binding.opensTextView.visibility = View.VISIBLE
                val openString = session.startDateTime.toJavaLocalDateTime().format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .withZone(ZoneId.systemDefault())
                )
                binding.opensTextView.text =
                    binding.dueTextView.context.getString(R.string.opens, openString)
            }
        }

        companion object {
            fun from(parent: ViewGroup): SessionHeaderViewHolder {

                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DueDateHeaderBinding.inflate(layoutInflater, parent, false)
                return SessionHeaderViewHolder(binding)

            }
        }
    }


    class AssessmentViewHolder(val binding: AssessmentCardBinding, private val onClick: (ScheduledAssessmentReference, ScheduledSessionWindow) -> Unit): RecyclerView.ViewHolder(binding.root) {

        private var currentItem: AssessmentItem? = null

        fun bind(assessmentItem: AssessmentItem) {
            currentItem = assessmentItem
            setupCard(assessmentItem.assessmentRef, assessmentItem.locked)
        }

        fun setupCard(assessmentRef: ScheduledAssessmentReference, locked: Boolean) {
            val context = binding.root.context
            if (locked) {
                binding.root.foreground = ColorDrawable(Color.parseColor("#B3FFFFFF"))
                binding.root.isEnabled = false
                binding.root.isClickable = false
            }
            val assessmentInfo = assessmentRef.assessmentInfo
            binding.title.text = assessmentInfo.label
            assessmentInfo.minutesToComplete?.let {
                binding.time.text = context.getString(R.string.number_minutes, it)
            }

            val foregroundColor = Color.parseColor(assessmentInfo.colorScheme?.foreground ?: "#000000")
            binding.imageBackground.background = ColorDrawable(foregroundColor)
            TodayFragment.assessmentIconMap.get(assessmentInfo.identifier)?.let {
                binding.assessmentImage.setImageDrawable(context.resources.getDrawable(it))
            }
        }

        init {
            itemView.setOnClickListener {
                currentItem?.let {
                    onClick(it.assessmentRef, it.session)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup, onClick: (ScheduledAssessmentReference, ScheduledSessionWindow) -> Unit): AssessmentViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AssessmentCardBinding.inflate(layoutInflater, parent, false)
                return AssessmentViewHolder(binding, onClick)
            }
        }
    }

}

class ItemDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}


sealed class DataItem {
    abstract val id: String
}

data class AssessmentItem(
    val assessmentRef: ScheduledAssessmentReference,
    val locked: Boolean,
    val session: ScheduledSessionWindow
) : DataItem() {
    override val id: String
        get() = assessmentRef.instanceGuid
}

data class SessionHeaderItem(val scheduledSessionWindow: ScheduledSessionWindow ): DataItem() {
    override val id: String
        get() = scheduledSessionWindow.instanceGuid
}