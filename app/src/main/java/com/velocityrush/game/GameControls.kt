package com.velocityrush.game

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class GameControls(
    private val context: Context,
    private val gameView: GameView,
    private val root: FrameLayout
) {

    private val info = TextView(context)

    private fun dp(value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    private fun makeButton(
        title: String,
        color: Int
    ): Button {
        return Button(context).apply {
            text = title
            textSize = 17f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD

            background = GradientDrawable().apply {
                setColor(color)
                cornerRadius = dp(18).toFloat()
                setStroke(dp(2), Color.WHITE)
            }

            isAllCaps = false
            stateListAnimator = null
        }
    }

    private fun addButton(
        title: String,
        color: Int,
        gravity: Int,
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        action: (Boolean) -> Unit
    ) {
        val button = makeButton(title, color)

        button.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    action(true)
                    view.isPressed = true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    action(false)
                    view.isPressed = false
                    view.performClick()
                }
            }
            true
        }

        val params = FrameLayout.LayoutParams(
            dp(width),
            dp(height),
            gravity
        )

        params.leftMargin = dp(left)
        params.topMargin = dp(top)

        root.addView(button, params)
    }

    fun install() {
        info.apply {
            text = "VELOCITY RUSH 3D"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
        }

        val infoParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            dp(48),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        )

        root.addView(info, infoParams)

        addButton(
            "◀",
            Color.rgb(25, 65, 110),
            Gravity.BOTTOM or Gravity.START,
            76, 68, 18, 0
        ) {
            gameView.setSteering(-1f, it)
        }

        addButton(
            "▶",
            Color.rgb(25, 65, 110),
            Gravity.BOTTOM or Gravity.START,
            76, 68, 104, 0
        ) {
            gameView.setSteering(1f, it)
        }

        addButton(
            "GAS",
            Color.rgb(20, 145, 75),
            Gravity.BOTTOM or Gravity.END,
            86, 76, 0, 0
        ) {
            gameView.setAccelerating(it)
        }

        addButton(
            "BRAKE",
            Color.rgb(185, 45, 45),
            Gravity.BOTTOM or Gravity.END,
            86, 68, 0, 84
        ) {
            gameView.setBraking(it)
        }

        addButton(
            "Ⅱ",
            Color.rgb(70, 70, 85),
            Gravity.TOP or Gravity.END,
            64, 52, 0, 12
        ) {
            if (it) {
                gameView.togglePause()
            }
        }
    }

    fun update(speed: Float, score: Int, paused: Boolean, crashed: Boolean) {
        val state = when {
            crashed -> "CRASHED"
            paused -> "PAUSED"
            else -> "RACING"
        }

        info.text = "$state   |   ${speed.toInt()} KM/H   |   SCORE $score"
    }
}
