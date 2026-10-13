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
    private var score = 0
    private var roadOffset = 0f

    private var steering = 0f
    private var accelerating = false
    private var braking = false
    private var paused = false
    private var crashed = false

    private var lastTime = System.nanoTime()

    private val opponents = mutableListOf<Opponent>()

    init {
        repeat(4) { index ->
            opponents.add(
                Opponent(
                    x = listOf(-1.5f, 0f, 1.5f)
                        .random(),
                    z = -12f - index * 9f
                )
            )
        }
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

        accelerating = x > 0.50f && y > 0.55f
        braking = x < 0.50f && y > 0.55f
    }

    fun update(): GameState {
        val now = System.nanoTime()

        val dt = min(
            0.05f,
            (now - lastTime) / 1_000_000_000f
        )

        lastTime = now

        if (!paused && !crashed) {
            val targetSpeed = when {
                braking -> 4f
                accelerating -> 28f
                else -> 15f
            }

            val change = if (speed < targetSpeed) {
                10f
            } else {
                15f
            }

            speed +=
                (targetSpeed - speed) *
                min(1f, change * dt)

            playerX += steering * dt * 2.4f

            playerX = playerX.coerceIn(
                -2.0f,
                2.0f
            )

            distance += speed * dt

            score = distance.toInt()

            roadOffset += speed * dt

            if (roadOffset > 10000f) {
                roadOffset = 0f
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

                if (
                    abs(car.x - playerX) < 0.7f &&
                    abs(car.z) < 1.2f
                ) {
                    crashed = true
                    speed = 0f
                }
            }
        }

        return GameState(
            playerX = playerX,
            speed = speed,
            distance = distance,
            score = score,
            roadOffset = roadOffset,
            opponents = opponents.map {
                Opponent(it.x, it.z)
            },
            crashed = crashed
        )
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
        lastTime = System.nanoTime()
    }

    fun restart() {
        playerX = 0f
        speed = 0f
        distance = 0f
        score = 0
        roadOffset = 0f
        crashed = false
        paused = false

        opponents.clear()

        repeat(4) { index ->
            opponents.add(
                Opponent(
                    x = listOf(-1.5f, 0f, 1.5f)
                        .random(),
                    z = -12f - index * 9f
                )
            )
        }

        lastTime = System.nanoTime()
    }
}
