package org.sagebionetworks.research.mtb.alpha_app.ui.study

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StudyInfoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is study info Fragment"
    }
    val text: LiveData<String> = _text
}