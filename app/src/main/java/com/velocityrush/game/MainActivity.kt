package com.velocityrush.game

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

class MainActivity : Activity() {

    private lateinit var gameView: GameView
    private lateinit var gameAudio: GameAudio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        hideSystemUI()

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        gameView = GameView(this)

        root.addView(
            gameView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        GameControls(
            context = this,
            gameView = gameView,
            root = root
        ).install()

        gameAudio = GameAudio()

        setContentView(root)
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemUI()
        }
    }

    override fun onPause() {
        if (::gameView.isInitialized) {
            gameView.onPause()
        }

        if (::gameAudio.isInitialized) {
            gameAudio.stop()
        }

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        if (::gameView.isInitialized) {
            gameView.onResume()
        }
    }

    override fun onDestroy() {
        if (::gameAudio.isInitialized) {
            gameAudio.release()
        }

        super.onDestroy()
    }
}
