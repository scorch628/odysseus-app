package com.odysseus.app.ui.settings

import com.odysseus.app.ui.base.UiIntent
import com.odysseus.app.ui.base.UiSideEffect
import com.odysseus.app.ui.base.UiState

sealed class SettingsIntent : UiIntent {
    data class LoadSettings(val apiUrl: String, val apiKey: String) : SettingsIntent()
    data class UpdateApiUrl(val apiUrl: String) : SettingsIntent()
    data class UpdateApiKey(val apiKey: String) : SettingsIntent()
    object SaveSettings : SettingsIntent()
}

data class SettingsState(
    val apiUrl: String = "",
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
) : UiState

sealed class SettingsEffect : UiSideEffect {
    data class ShowToast(val message: String) : SettingsEffect()
    object NavigateBack : SettingsEffect()
}
