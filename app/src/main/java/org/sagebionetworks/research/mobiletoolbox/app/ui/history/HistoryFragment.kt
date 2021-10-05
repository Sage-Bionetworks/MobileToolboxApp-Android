package org.sagebionetworks.research.mobiletoolbox.app.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionWindow
import org.sagebionetworks.research.mobiletoolbox.app.MtbBaseFragment
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentTodayListBinding

class HistoryFragment : MtbBaseFragment() {

    private val viewModel: HistoryViewModel by viewModel()
    lateinit var binding: FragmentTodayListBinding
    lateinit var listAdapter: HistoryRecyclerViewAdapter
    lateinit var headerAdapter: HistoryHeaderAdapter

    val adherenceRecordRepo: AdherenceRecordRepo by inject()
    val authRepo: AuthenticationRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.sessionLiveData.observe(this, Observer {
            when(it) {
                is ResourceResult.Success -> {
                    sessionsLoaded(it.data.scheduledSessionWindows)
                }
                is ResourceResult.InProgress -> {

                }
                is ResourceResult.Failed -> {

                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodayListBinding.inflate(inflater)
        listAdapter = HistoryRecyclerViewAdapter()
        headerAdapter = HistoryHeaderAdapter()
        headerAdapter.loading = true

        // Set the adapter
        with(binding.list) {
            layoutManager =  LinearLayoutManager(context)
            adapter = ConcatAdapter(headerAdapter, listAdapter)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTodaysSessions()
    }

    private fun sessionsLoaded(sessions: List<ScheduledSessionWindow>) {
        val records = sessions.flatMap {
            it.assessments.flatMap { assessment -> assessment.history() }
        }.sortedBy { it.finishedOn }

        var minutes = records.sumOf { it.minutes }

        listAdapter.submitList(records)

        headerAdapter.loading = false
        headerAdapter.minutes = minutes
        headerAdapter.notifyDataSetChanged()

    }

}