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
        val width = width.toFloat()
        val height = height.toFloat()

        if (width <= 0f || height <= 0f) {
            return true
        }

        val x = event.x / width
        val y = event.y / height

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                queueEvent {
                    gameRenderer.handleTouch(
                        x,
                        y,
                        true
                    )
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                queueEvent {
                    gameRenderer.handleTouch(
                        x,
                        y,
                        false
                    )
                }
            }
        }

        return true
    }

    override fun onPause() {
        super.onPause()

        queueEvent {
            gameRenderer.pauseGame()
        }
    }

    override fun onResume() {
        super.onResume()
        super.requestRender()
    }
}
