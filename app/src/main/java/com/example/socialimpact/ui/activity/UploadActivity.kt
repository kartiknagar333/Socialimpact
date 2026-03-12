package com.example.socialimpact.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.socialimpact.di.component.SocialImpactApp
import com.example.socialimpact.ui.layouts.PostHelpRequestLayout
import com.example.socialimpact.ui.theme.SocialimpactTheme
import com.example.socialimpact.ui.viewmodel.UploadViewModel
import com.example.socialimpact.ui.viewmodel.UploadViewModelFactory
import javax.inject.Inject

class UploadActivity : ComponentActivity() {

    @Inject
    lateinit var uploadViewModelFactory: UploadViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as SocialImpactApp)
            .appComponent
            .uploadActivityComponent()
            .create()
            .inject(this)

        enableEdgeToEdge()
        setContent {
            SocialimpactTheme {
                val viewModel: UploadViewModel = viewModel(factory = uploadViewModelFactory)
                val uiState by viewModel.uiState.collectAsState()

                PostHelpRequestLayout(
                    uiState = uiState,
                    onTitleChange = viewModel::onTitleChange,
                    onDescriptionChange = viewModel::onDescriptionChange,
                    onFundAmountChange = viewModel::onFundAmountChange,
                    onAddressChange = viewModel::onAddressChange,
                    onStartDateChange = viewModel::onStartDateChange,
                    onEndDateChange = viewModel::onEndDateChange,
                    onToggleNeed = viewModel::toggleNeed,           // not onToggleNeed
                    onToggleCategory = viewModel::toggleCategory,   // not onToggleCategory
                    onAddDynamicNeed = viewModel::addDynamicNeed,   // not onAddDynamicNeed
                    onRemoveDynamicNeed = viewModel::removeDynamicNeed,
                    onUpdateDynamicNeed = viewModel::updateDynamicNeed,
                    onBack = { finish() },
                    onDone = { /* TODO: add onDone() to ViewModel */ }
                )
            }
        }
    }
}