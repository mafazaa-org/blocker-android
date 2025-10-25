package com.mafazaa.ainaa.helpers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

/**
 * A custom LifecycleOwner and SavedStateRegistryOwner implementation.
 * it's for using compose in overlay window
 */
class MyLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {


private val savedStateController = SavedStateRegistryController.create(this)
private val lifecycleRegistry = LifecycleRegistry(this)

init {
    // Attach and restore during initialization to avoid:
    // "Restarter must be created only during owner's initialization stage"
    savedStateController.performAttach()
    savedStateController.performRestore(null)
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
}

override val lifecycle: Lifecycle
    get() = lifecycleRegistry

fun handleLifecycleEvent(event: Event) {
    lifecycleRegistry.handleLifecycleEvent(event)
}

override val savedStateRegistry: SavedStateRegistry
    get() = savedStateController.savedStateRegistry

fun onStart() {
    handleLifecycleEvent(Event.ON_START)
    handleLifecycleEvent(Event.ON_RESUME)
}
    fun onDestroy() {

        handleLifecycleEvent(Event.ON_PAUSE)
        handleLifecycleEvent(Event.ON_STOP)
        handleLifecycleEvent(Event.ON_DESTROY)
    }

}