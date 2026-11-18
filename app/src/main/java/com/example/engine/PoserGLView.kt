package com.example.engine

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.hypot

@SuppressLint("ClickableViewAccessibility")
@Composable
fun PoserGLView(
    renderSnapshot: StudioRenderSnapshot,
    modifier: Modifier = Modifier,
    onTapHit: (PickHit) -> Unit,
    onOrbitCamera: (dx: Float, dy: Float) -> Unit,
    onZoomCamera: (scaleFactor: Float) -> Unit,
    onPanCamera: (dx: Float, dy: Float) -> Unit,
    onGizmoDragStart: (axis: Int) -> Unit,
    onGizmoDrag: (axis: Int, dx: Float, dy: Float, viewProj: FloatArray, viewW: Float, viewH: Float) -> Unit,
    onGizmoDragEnd: () -> Unit
) {
    val context = LocalContext.current
    val renderer = remember { PoserGLRenderer() }
    val glView = remember {
        GLSurfaceView(context).apply {
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    LaunchedEffect(renderSnapshot) {
        renderer.snapshot = renderSnapshot
    }

    DisposableEffect(glView) {
        onDispose {
            glView.onPause()
        }
    }

    val gestureState = remember {
        object {
            var activeAxis: Int = -1
            var isTapCandidate: Boolean = false
            var downX = 0f
            var downY = 0f
            var lastX = 0f
            var lastY = 0f
            var prevPinchDist = 0f
            var prevMidX = 0f
            var prevMidY = 0f
            var wasTwoFinger = false
        }
    }

    AndroidView(
        factory = {
            glView.setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        gestureState.downX = event.x
                        gestureState.downY = event.y
                        gestureState.lastX = event.x
                        gestureState.lastY = event.y
                        gestureState.isTapCandidate = true
                        gestureState.wasTwoFinger = false

                        val hit = renderer.pickAt(event.x, event.y, includeGizmo = true)
                        if (hit is PickHit.GizmoAxis) {
                            gestureState.activeAxis = hit.axis
                            onGizmoDragStart(hit.axis)
                        } else {
                            gestureState.activeAxis = -1
                        }
                        true
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if (event.pointerCount >= 2) {
                            gestureState.isTapCandidate = false
                            if (gestureState.activeAxis != -1) {
                                gestureState.activeAxis = -1
                                onGizmoDragEnd()
                            }
                            gestureState.wasTwoFinger = true
                            val dx = event.getX(0) - event.getX(1)
                            val dy = event.getY(0) - event.getY(1)
                            gestureState.prevPinchDist = hypot(dx, dy).coerceAtLeast(1f)
                            gestureState.prevMidX = (event.getX(0) + event.getX(1)) * 0.5f
                            gestureState.prevMidY = (event.getY(0) + event.getY(1)) * 0.5f
                        }
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (event.pointerCount >= 2) {
                            val dx = event.getX(0) - event.getX(1)
                            val dy = event.getY(0) - event.getY(1)
                            val dist = hypot(dx, dy).coerceAtLeast(1f)
                            val midX = (event.getX(0) + event.getX(1)) * 0.5f
                            val midY = (event.getY(0) + event.getY(1)) * 0.5f

                            if (gestureState.wasTwoFinger) {
                                val zoomFactor = dist / gestureState.prevPinchDist.coerceAtLeast(1f)
                                if (kotlin.math.abs(zoomFactor - 1f) > 0.002f) {
                                    onZoomCamera(zoomFactor)
                                }
                                val panDx = midX - gestureState.prevMidX
                                val panDy = midY - gestureState.prevMidY
                                onPanCamera(panDx, panDy)
                            }
                            gestureState.prevPinchDist = dist
                            gestureState.prevMidX = midX
                            gestureState.prevMidY = midY
                            gestureState.wasTwoFinger = true
                        } else if (!gestureState.wasTwoFinger) {
                            val moveDx = event.x - gestureState.lastX
                            val moveDy = event.y - gestureState.lastY
                            val totalMoved = hypot(event.x - gestureState.downX, event.y - gestureState.downY)
                            if (totalMoved > 12f) {
                                gestureState.isTapCandidate = false
                            }
                            if (gestureState.activeAxis != -1) {
                                val (vw, vh) = renderer.getViewSize()
                                onGizmoDrag(
                                    gestureState.activeAxis,
                                    moveDx,
                                    moveDy,
                                    renderer.getLastViewProj(),
                                    vw,
                                    vh
                                )
                            } else if (!gestureState.isTapCandidate) {
                                onOrbitCamera(moveDx, moveDy)
                            }
                            gestureState.lastX = event.x
                            gestureState.lastY = event.y
                        }
                        true
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        if (event.pointerCount <= 2) {
                            val remainingIdx = if (event.actionIndex == 0) 1 else 0
                            if (remainingIdx < event.pointerCount) {
                                gestureState.lastX = event.getX(remainingIdx)
                                gestureState.lastY = event.getY(remainingIdx)
                            }
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (gestureState.activeAxis != -1) {
                            gestureState.activeAxis = -1
                            onGizmoDragEnd()
                        } else if (gestureState.isTapCandidate && event.actionMasked == MotionEvent.ACTION_UP) {
                            val hit = renderer.pickAt(event.x, event.y, includeGizmo = false)
                            onTapHit(hit)
                        }
                        gestureState.wasTwoFinger = false
                        true
                    }
                    else -> false
                }
            }
            glView
        },
        modifier = modifier
    )
}
