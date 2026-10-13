package com.velocityrush.game

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class Opponent(
    var x: Float,
    var z: Float
)

data class GameState(
    val playerX: Float,
    val speed: Float,
    val distance: Float,
    val score: Int,
    val roadOffset: Float,
    val opponents: List<Opponent>,
    val crashed: Boolean
)

class GameLogic {

    private var playerX = 0f
    private var speed = 0f
    private var distance = 0f
    private var roadOffset = 0f

    private var steering = 0f
    private var accelerating = false
    private var braking = false

    private var paused = false
    private var crashed = false

    private var lastTime = System.nanoTime()

    private val opponents = mutableListOf<Opponent>()

    init {
        resetOpponents()
    }

    private fun resetOpponents() {
        opponents.clear()

        repeat(4) { index ->
            opponents.add(
                Opponent(
                    x = listOf(-1.5f, 0f, 1.5f).random(),
                    z = -12f - index * 9f
                )
            )
        }
    }

    fun setSteering(direction: Float) {
        steering = direction.coerceIn(-1f, 1f)
    }

    fun setAccelerating(pressed: Boolean) {
        accelerating = pressed
    }

    fun setBraking(pressed: Boolean) {
        braking = pressed
    }

    fun togglePause() {
        if (!crashed) {
            paused = !paused
            lastTime = System.nanoTime()
        }
    }

    fun pause() {
        paused = true
        lastTime = System.nanoTime()
    }

    fun resume() {
        paused = false
        lastTime = System.nanoTime()
    }

    fun restart() {
        playerX = 0f
        speed = 0f
        distance = 0f
        roadOffset = 0f

        steering = 0f
        accelerating = false
        braking = false

        paused = false
        crashed = false

        resetOpponents()

        lastTime = System.nanoTime()
    }

    fun handleTouch(
        x: Float,
        y: Float,
        pressed: Boolean
    ) {
        if (!pressed) {
            steering = 0f
            accelerating = false
            braking = false
            return
        }

        steering = when {
            x < 0.25f -> -1f
            x > 0.75f -> 1f
            else -> 0f
        }

        accelerating = x > 0.5f && y > 0.55f
        braking = x < 0.5f && y > 0.55f
    }

    fun update(): GameState {
        val now = System.nanoTime()

        val dt = min(
            0.05f,
            (now - lastTime) / 1_000_000_000f
        ).coerceAtLeast(0f)

        lastTime = now

        if (!paused && !crashed) {

            val targetSpeed = when {
                braking -> 3f
                accelerating -> 32f
                else -> 15f
            }

            val accelerationRate = when {
                braking -> 9f
                speed < targetSpeed -> 5f
                else -> 3f
            }

            val difference = targetSpeed - speed

            speed += difference * min(
                1f,
                accelerationRate * dt
            )

            speed = max(0f, speed)

            playerX += steering * dt * (
                1.4f + speed * 0.045f
            )

            playerX = playerX.coerceIn(-2.1f, 2.1f)

            distance += speed * dt

            roadOffset += speed * dt

            if (roadOffset > 10000f) {
                roadOffset %= 100f
            }

            for (car in opponents) {

                car.z += speed * dt * 0.55f

                if (car.z > 2f) {
                    car.z = -35f -
                        Random.nextFloat() * 15f

                    car.x = listOf(
                        -1.5f,
                        0f,
                        1.5f
                    ).random()
                }

                val sameLane =
                    abs(car.x - playerX) < 0.72f

                val collisionDistance =
                    abs(car.z) < 1.15f

                if (sameLane && collisionDistance) {
                    crashed = true
                    speed = 0f
                    break
                }
            }
        }

        return GameState(
            playerX = playerX,
            speed = speed,
            distance = distance,
            score = distance.toInt(),
            roadOffset = roadOffset,
            opponents = opponents.map {
                Opponent(it.x, it.z)
            },
            crashed = crashed
        )
    }
}
