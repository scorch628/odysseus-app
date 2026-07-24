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
            val user = repository.username.first()
            val pass = repository.password.first()
            setIntent(SettingsIntent.LoadSettings(url, key, user, pass))
        }
    }

    override suspend fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.LoadSettings -> {
                setState {
                    copy(
                        apiUrl = intent.apiUrl,
                        apiKey = intent.apiKey,
                        username = intent.username,
                        password = intent.password,
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
            is SettingsIntent.UpdateUsername -> {
                setState { copy(username = intent.username) }
            }
            is SettingsIntent.UpdatePassword -> {
                setState { copy(password = intent.password) }
            }
            SettingsIntent.SaveSettings -> {
                hapticManager.vibrateCopy()
                val currentUrl = state.value.apiUrl
                val currentKey = state.value.apiKey
                val currentUsername = state.value.username
                val currentPassword = state.value.password
                repository.saveSettings(currentUrl, currentKey, currentUsername, currentPassword)
                setEffect(SettingsEffect.ShowToast("Settings saved!"))
                setEffect(SettingsEffect.NavigateBack)
            }
        }
    }
}
