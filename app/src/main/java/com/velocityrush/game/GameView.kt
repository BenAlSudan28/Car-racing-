package com.velocityrush.game

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class GameView(context: Context) : GLSurfaceView(context) {

    private val gameRenderer: GameRenderer

    init {
        setEGLContextClientVersion(2)

        gameRenderer = GameRenderer(context)
        setRenderer(gameRenderer)

        renderMode = RENDERMODE_CONTINUOUSLY

        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val w = width.toFloat()
        val h = height.toFloat()

        if (w <= 0f || h <= 0f) return true

        val x = event.x / w
        val y = event.y / h

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                queueEvent {
                    gameRenderer.handleTouch(x, y, true)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                queueEvent {
                    gameRenderer.handleTouch(x, y, false)
                }
            }
        }

        return true
    }

    fun setSteering(direction: Float, pressed: Boolean) {
        queueEvent {
            gameRenderer.setSteering(
                if (pressed) direction else 0f
            )
        }
    }

    fun setAccelerating(pressed: Boolean) {
        queueEvent {
            gameRenderer.setAccelerating(pressed)
        }
    }

    fun setBraking(pressed: Boolean) {
        queueEvent {
            gameRenderer.setBraking(pressed)
        }
    }

    fun togglePause() {
        queueEvent {
            gameRenderer.togglePause()
        }
    }

    override fun onPause() {
        queueEvent {
            gameRenderer.pauseGame()
        }

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        queueEvent {
            gameRenderer.resumeGame()
        }
    }
}
