package com.velocityrush.game

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import kotlin.math.max
import kotlin.random.Random

class GameRenderer(
    private val context: Context
) : GLSurfaceView.Renderer {

    private val logic = GameLogic()

    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var matrixHandle = 0

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val temp = FloatArray(16)
    private val mvp = FloatArray(16)

    private val vertices = floatArrayOf(
        -0.5f,-0.5f,-0.5f,
         0.5f,-0.5f,-0.5f,
         0.5f, 0.5f,-0.5f,
        -0.5f, 0.5f,-0.5f,
        -0.5f,-0.5f, 0.5f,
         0.5f,-0.5f, 0.5f,
         0.5f, 0.5f, 0.5f,
        -0.5f, 0.5f, 0.5f
    )

    private val indices = shortArrayOf(
        0,1,2, 0,2,3,
        4,6,5, 4,7,6,
        0,4,5, 0,5,1,
        3,2,6, 3,6,7,
        1,5,6, 1,6,2,
        0,3,7, 0,7,4
    )

    private val vertexBuffer =
        java.nio.ByteBuffer.allocateDirect(vertices.size * 4)
            .order(java.nio.ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

    private val indexBuffer =
        java.nio.ByteBuffer.allocateDirect(indices.size * 2)
            .order(java.nio.ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indices)
                position(0)
            }

    private val vertexShader = """
        attribute vec3 aPosition;
        uniform mat4 uMVP;
        void main() {
            gl_Position = uMVP * vec4(aPosition, 1.0);
        }
    """.trimIndent()

    private val fragmentShader = """
        precision mediump float;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }
    """.trimIndent()

    override fun onSurfaceCreated(
        gl: javax.microedition.khronos.opengles.GL10?,
        config: javax.microedition.khronos.egl.EGLConfig?
    ) {
        GLES20.glClearColor(0.45f, 0.72f, 0.95f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        program = createProgram(vertexShader, fragmentShader)

        positionHandle = GLES20.glGetAttribLocation(
            program, "aPosition"
        )

        colorHandle = GLES20.glGetUniformLocation(
            program, "uColor"
        )

        matrixHandle = GLES20.glGetUniformLocation(
            program, "uMVP"
        )
    }

    override fun onSurfaceChanged(
        gl: javax.microedition.khronos.opengles.GL10?,
        width: Int,
        height: Int
    ) {
        GLES20.glViewport(0, 0, width, height)

        Matrix.perspectiveM(
            projection, 0, 60f,
            width.toFloat() / max(1, height),
            0.1f, 100f
        )
    }

    override fun onDrawFrame(
        gl: javax.microedition.khronos.opengles.GL10?
    ) {
        GLES20.glClear(
            GLES20.GL_COLOR_BUFFER_BIT or
                GLES20.GL_DEPTH_BUFFER_BIT
        )

        val state = logic.update()

        Matrix.setLookAtM(
            view, 0,
            state.playerX * 0.25f, 3.0f, 7.0f,
            0f, 0f, -5f,
            0f, 1f, 0f
        )

        drawBox(
            0f, -0.15f, -8f,
            5.5f, 0.2f, 32f,
            floatArrayOf(0.12f, 0.13f, 0.15f, 1f)
        )

        drawBox(
            -3f, 0f, -8f,
            0.35f, 0.25f, 32f,
            floatArrayOf(0.85f, 0.2f, 0.15f, 1f)
        )

        drawBox(
            3f, 0f, -8f,
            0.35f, 0.25f, 32f,
            floatArrayOf(0.85f, 0.2f, 0.15f, 1f)
        )

        for (i in 0 until 20) {
            val z = -i * 1.6f + state.roadOffset % 1.6f

            drawBox(
                -0.95f, -0.025f, z,
                0.07f, 0.025f, 0.75f,
                floatArrayOf(1f, 1f, 1f, 1f)
            )

            drawBox(
                0.95f, -0.025f, z,
                0.07f, 0.025f, 0.75f,
                floatArrayOf(1f, 1f, 1f, 1f)
            )
        }

        state.opponents.forEach {
            drawCar(
                it.x, it.z,
                floatArrayOf(0.9f, 0.1f, 0.08f, 1f)
            )
        }

        drawCar(
            state.playerX, 0f,
            floatArrayOf(0.05f, 0.3f, 1f, 1f)
        )
    }

    private fun drawCar(x: Float, z: Float, color: FloatArray) {
        drawBox(
            x, 0.28f, z,
            0.75f, 0.38f, 1.25f, color
        )

        drawBox(
            x, 0.53f, z + 0.05f,
            0.46f, 0.28f, 0.58f,
            floatArrayOf(0.1f, 0.18f, 0.23f, 1f)
        )

        for (side in listOf(-1f, 1f)) {
            drawBox(
                x + side * 0.39f, 0.17f, z - 0.35f,
                0.12f, 0.22f, 0.28f,
                floatArrayOf(0.04f, 0.04f, 0.04f, 1f)
            )

            drawBox(
                x + side * 0.39f, 0.17f, z + 0.35f,
                0.12f, 0.22f, 0.28f,
                floatArrayOf(0.04f, 0.04f, 0.04f, 1f)
            )
        }
    }

    private fun drawBox(
        x: Float, y: Float, z: Float,
        sx: Float, sy: Float, sz: Float,
        color: FloatArray
    ) {
        Matrix.setIdentityM(model, 0)
        Matrix.translateM(model, 0, x, y, z)
        Matrix.scaleM(model, 0, sx, sy, sz)

        Matrix.multiplyMM(temp, 0, view, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, projection, 0, temp, 0)

        GLES20.glUseProgram(program)

        vertexBuffer.position(0)

        GLES20.glVertexAttribPointer(
            positionHandle, 3,
            GLES20.GL_FLOAT, false, 0,
            vertexBuffer
        )

        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glUniformMatrix4fv(
            matrixHandle, 1, false, mvp, 0
        )

        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        indexBuffer.position(0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )
    }

    fun handleTouch(x: Float, y: Float, pressed: Boolean) {
        logic.handleTouch(x, y, pressed)
    }

    fun setSteering(direction: Float) {
        logic.setSteering(direction)
    }

    fun setAccelerating(pressed: Boolean) {
        logic.setAccelerating(pressed)
    }

    fun setBraking(pressed: Boolean) {
        logic.setBraking(pressed)
    }

    fun togglePause() {
        logic.togglePause()
    }

    fun pauseGame() {
        logic.pause()
    }

    fun resumeGame() {
        logic.resume()
    }

    private fun createProgram(vertex: String, fragment: String): Int {
        val vs = loadShader(GLES20.GL_VERTEX_SHADER, vertex)
        val fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment)

        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
        }
    }

    private fun loadShader(type: Int, source: String): Int {
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, source)
            GLES20.glCompileShader(it)
        }
    }
}
