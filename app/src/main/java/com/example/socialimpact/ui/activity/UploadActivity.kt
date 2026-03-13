package com.example.socialimpact.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
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

                LaunchedEffect(uiState.isSuccess) {
                    if (uiState.isSuccess) {
                        Toast.makeText(this@UploadActivity, "Post Created Successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                LaunchedEffect(uiState.error) {
                    uiState.error?.let {
                        Toast.makeText(this@UploadActivity, it, Toast.LENGTH_LONG).show()
                    }
                }

                PostHelpRequestLayout(
                    uiState = uiState,
                    onTitleChange = viewModel::onTitleChange,
                    onDescriptionChange = viewModel::onDescriptionChange,
                    onFundAmountChange = viewModel::onFundAmountChange,
                    onAddressChange = viewModel::onAddressChange,
                    onStartDateChange = viewModel::onStartDateChange,
                    onEndDateChange = viewModel::onEndDateChange,
                    onToggleNeed = viewModel::toggleNeed,
                    onToggleCategory = viewModel::toggleCategory,
                    onAddDynamicNeed = viewModel::addDynamicNeed,
                    onRemoveDynamicNeed = viewModel::removeDynamicNeed,
                    onUpdateDynamicNeed = viewModel::updateDynamicNeed,
                    onBack = { finish() },
                    onDone = { 
                        viewModel.uploadPost()
                    }
                )
            }
        }
    }
}
