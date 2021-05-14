package org.sagebionetworks.research.mtb.alpha_app.ui.today

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.widget.TextViewCompat
import org.sagebionetworks.bridge.kmm.shared.models.AssessmentInfo
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledAssessmentReference
import org.sagebionetworks.research.mtb.alpha_app.R
import org.sagebionetworks.research.mtb.alpha_app.databinding.AssessmentCardBinding


class AssessmentCard : CardView {

    lateinit var binding: AssessmentCardBinding

    constructor(context: Context) : super(context){
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val view = View.inflate(context, R.layout.assessment_card, this)
        binding = AssessmentCardBinding.bind(view)
    }

    fun setupCard(assessmentRef: ScheduledAssessmentReference) {
        val assessmentInfo = assessmentRef.assessmentInfo
        binding.title.text = assessmentInfo.label
        assessmentInfo.minutesToComplete?.let {
            binding.time.text = context.getString(R.string.number_minutes, it)
        }
        val foregroundColor = Color.parseColor(assessmentInfo.colorScheme?.foreground)
        binding.imageBackground.background = ColorDrawable(foregroundColor)
        TodayFragment.assessmentIconMap.get(assessmentInfo.identifier)?.let {
            binding.assessmentImage.setImageDrawable(resources.getDrawable(it))
        }
        val inactivatedColor = Color.parseColor(assessmentInfo.colorScheme?.inactivated)
        TextViewCompat.setCompoundDrawableTintList(binding.title, ColorStateList.valueOf(inactivatedColor))
        TextViewCompat.setCompoundDrawableTintMode(binding.title, PorterDuff.Mode.MULTIPLY)
    }


}