package org.sagebionetworks.research.mobiletoolbox.app.ui.account

import androidx.lifecycle.ViewModel
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository

class AccountViewModel(val authRepo: AuthenticationRepository) : ViewModel()