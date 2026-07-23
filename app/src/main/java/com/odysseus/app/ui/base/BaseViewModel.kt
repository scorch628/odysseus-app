package com.odysseus.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : UiState, Intent : UiIntent, Effect : UiSideEffect> : ViewModel() {

    private val initialState: State by lazy { createInitialState() }
    abstract fun createInitialState(): State

    private val _state: MutableStateFlow<State> by lazy { MutableStateFlow(initialState) }
    val state: StateFlow<State> by lazy { _state.asStateFlow() }

    private val _intent = MutableSharedFlow<Intent>()

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            _intent.collect { intent ->
                handleIntent(intent)
            }
        }
    }

    fun setIntent(intent: Intent) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    protected abstract suspend fun handleIntent(intent: Intent)

    protected fun setState(reducer: State.() -> State) {
        _state.update(reducer)
    }

    protected fun setEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}
