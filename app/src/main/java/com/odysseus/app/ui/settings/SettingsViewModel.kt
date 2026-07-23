package com.odysseus.app.ui.settings

import androidx.lifecycle.viewModelScope
import com.odysseus.app.data.SettingsRepository
import com.odysseus.app.haptics.HapticManager
import com.odysseus.app.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val hapticManager: HapticManager
) : BaseViewModel<SettingsState, SettingsIntent, SettingsEffect>() {

    override fun createInitialState(): SettingsState = SettingsState(isLoading = true)

    init {
        viewModelScope.launch {
            val url = repository.apiUrl.first()
            val key = repository.apiKey.first()
            setIntent(SettingsIntent.LoadSettings(url, key))
        }
    }

    override suspend fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> {
                setState {
                    copy(
                        apiUrl = intent.apiUrl,
                        apiKey = intent.apiKey,
                        isLoading = false
                    )
                }
            }
            is SettingsIntent.UpdateApiUrl -> {
                setState { copy(apiUrl = intent.apiUrl) }
            }
            is SettingsIntent.UpdateApiKey -> {
                setState { copy(apiKey = intent.apiKey) }
            }
            SettingsIntent.SaveSettings -> {
                hapticManager.vibrateCopy()
                val currentUrl = state.value.apiUrl
                val currentKey = state.value.apiKey
                repository.saveSettings(currentUrl, currentKey)
                setEffect(SettingsEffect.ShowToast("Settings saved!"))
                setEffect(SettingsEffect.NavigateBack)
            }
        }
    }
}
