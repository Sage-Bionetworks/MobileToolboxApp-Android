package org.sagebionetworks.research.mtb.alpha_app.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.sagebionetworks.research.mtb.alpha_app.databinding.FragmentStudyInfoBinding

class StudyInfoFragment : Fragment() {

    private lateinit var studyInfoViewModel: StudyInfoViewModel
    private var _binding: FragmentStudyInfoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        studyInfoViewModel =
            ViewModelProvider(this).get(StudyInfoViewModel::class.java)

        _binding = FragmentStudyInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        studyInfoViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}