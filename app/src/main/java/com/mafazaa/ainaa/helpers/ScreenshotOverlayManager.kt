package com.mafazaa.ainaa.helpers

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.ui.overlay.ScreenshotOverlay
import com.mafazaa.ainaa.ui.theme.AinaaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * ScreenshotOverlayManager manages the floating screenshot overlay using Jetpack Compose.
 * It provides methods to show and close the overlay, and handles drag, close, and screenshot actions.
 * This class is not a Service and should be managed by the caller.
 */
class ScreenshotOverlayManager(val context: Context) {
    private var overlayView: View? = null
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val myLifecycleOwner = MyLifecycleOwner()

    /**
     * Shows the screenshot overlay if not already visible.
     */
    fun showOverlay() {
        Log.d(TAG, "showOverlay")
        if (overlayView != null) return
        myLifecycleOwner.onStart()
        myLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        myLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        overlayView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(myLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(myLifecycleOwner)
            setContent {
                AinaaTheme {
                    ScreenshotOverlay(
                        modifier = Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    val layoutParams =
                                        overlayView!!.layoutParams as WindowManager.LayoutParams
                                    layoutParams.x += dragAmount.x.toInt()
                                    layoutParams.y += dragAmount.y.toInt()
                                    windowManager.updateViewLayout(overlayView, layoutParams)
                                    change.consume()
                                }
                            )
                        },
                        onClose = { closeOverlay() },
                        onScreenShot = { d ->
                            serviceScope.launch {
                                delay(d)
                                context.startAccessibilityService(MyAccessibilityService.Companion.ACTION_SHARE_CURRENT_SCREEN)
                            }
                        })
                }
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }
        windowManager.addView(overlayView, params)
        isShowing.value = true
    }

    /**
     * Closes the screenshot overlay if visible.
     */
    fun closeOverlay() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
        myLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        myLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        isShowing.value = false
    }

    companion object {
        private const val initialX = -100
        private const val initialY = 800
        const val TAG = "ScreenshotOverlayManager"
        var isShowing = MutableStateFlow(false)
    }
}
