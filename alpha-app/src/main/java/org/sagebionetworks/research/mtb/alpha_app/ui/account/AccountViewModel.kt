package org.sagebionetworks.research.mtb.alpha_app.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is account Fragment"
    }
    val text: LiveData<String> = _text
}