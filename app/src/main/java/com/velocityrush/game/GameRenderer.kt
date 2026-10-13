package com.velocityrush.game

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GameRenderer(
    private val context: Context
) : GLSurfaceView.Renderer {

    private val logic = GameLogic()

    private var width = 1
    private var height = 1

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val mvp = FloatArray(16)

    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var matrixHandle = 0

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

    private val cubeVertices = floatArrayOf(
        -0.5f,-0.5f,-0.5f,  0.5f,-0.5f,-0.5f,
         0.5f, 0.5f,-0.5f, -0.5f, 0.5f,-0.5f,
        -0.5f,-0.5f, 0.5f,  0.5f,-0.5f, 0.5f,
         0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f
    )

    private val indices = shortArrayOf(
        0,1,2, 0,2,3,
        4,6,5, 4,7,6,
        0,4,5, 0,5,1,
        3,2,6, 3,6,7,
        1,5,6, 1,6,2,
        0,3,7, 0,7,4
    )

    private val cubeBuffer = java.nio.ByteBuffer
        .allocateDirect(cubeVertices.size * 4)
        .order(java.nio.ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(cubeVertices)
            position(0)
        }

    private val indexBuffer = java.nio.ByteBuffer
        .allocateDirect(indices.size * 2)
        .order(java.nio.ByteOrder.nativeOrder())
        .asShortBuffer()
        .apply {
            put(indices)
            position(0)
        }

    override fun onSurfaceCreated(
        gl: javax.microedition.khronos.opengles.GL10?,
        config: javax.microedition.khronos.egl.EGLConfig?
    ) {
        GLES20.glClearColor(0.40f, 0.70f, 0.95f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        program = createProgram(
            vertexShader,
            fragmentShader
        )

        positionHandle = GLES20.glGetAttribLocation(
            program,
            "aPosition"
        )

        colorHandle = GLES20.glGetUniformLocation(
            program,
            "uColor"
        )

        matrixHandle = GLES20.glGetUniformLocation(
            program,
            "uMVP"
        )
    }

    override fun onSurfaceChanged(
        gl: javax.microedition.khronos.opengles.GL10?,
        w: Int,
        h: Int
    ) {
        width = max(1, w)
        height = max(1, h)

        GLES20.glViewport(0, 0, width, height)

        Matrix.perspectiveM(
            projection,
            0,
            60f,
            width.toFloat() / height.toFloat(),
            0.1f,
            100f
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
            view,
            0,
            state.playerX * 0.35f,
            3.0f,
            7.0f,
            0f,
            0f,
            -5f,
            0f,
            1f,
            0f
        )

        drawBox(
            0f, -0.15f, -8f,
            5f, 0.2f, 30f,
            floatArrayOf(0.16f, 0.17f, 0.19f, 1f)
        )

        drawRoadMarkings(state.roadOffset)

        drawBox(
            -2.8f, 0.1f, -8f,
            0.18f, 0.35f, 30f,
            floatArrayOf(0.9f, 0.9f, 0.9f, 1f)
        )

        drawBox(
            2.8f, 0.1f, -8f,
            0.18f, 0.35f, 30f,
            floatArrayOf(0.9f, 0.9f, 0.9f, 1f)
        )

        for (car in state.opponents) {
            drawCar(
                car.x,
                car.z,
                floatArrayOf(0.9f, 0.12f, 0.1f, 1f)
            )
        }

        drawCar(
            state.playerX,
            0f,
            floatArrayOf(0.05f, 0.35f, 1f, 1f)
        )
    }

    private fun drawRoadMarkings(offset: Float) {
        for (i in 0 until 18) {
            val z = -i * 1.7f + (offset % 1.7f)

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
    }

    private fun drawCar(
        x: Float,
        z: Float,
        color: FloatArray
    ) {
        drawBox(
            x, 0.28f, z,
            0.75f, 0.38f, 1.25f,
            color
        )

        drawBox(
            x, 0.54f, z + 0.05f,
            0.48f, 0.28f, 0.58f,
            floatArrayOf(0.12f, 0.18f, 0.23f, 1f)
        )

        for (side in listOf(-1f, 1f)) {
            drawBox(
                x + side * 0.39f,
                0.17f,
                z - 0.35f,
                0.12f,
                0.22f,
                0.28f,
                floatArrayOf(0.06f, 0.06f, 0.07f, 1f)
            )

            drawBox(
                x + side * 0.39f,
                0.17f,
                z + 0.35f,
                0.12f,
                0.22f,
                0.28f,
                floatArrayOf(0.06f, 0.06f, 0.07f, 1f)
            )
        }
    }

    private fun drawBox(
        x: Float,
        y: Float,
        z: Float,
        sx: Float,
        sy: Float,
        sz: Float,
        color: FloatArray
    ) {
        Matrix.setIdentityM(model, 0)

        Matrix.translateM(model, 0, x, y, z)
        Matrix.scaleM(model, 0, sx, sy, sz)

        Matrix.multiplyMM(mvp, 0, view, 0, model, 0)

        val finalMatrix = FloatArray(16)

        Matrix.multiplyMM(
            finalMatrix,
            0,
            projection,
            0,
            mvp,
            0
        )

        GLES20.glUseProgram(program)

        cubeBuffer.position(0)

        GLES20.glVertexAttribPointer(
            positionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            cubeBuffer
        )

        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glUniformMatrix4fv(
            matrixHandle,
            1,
            false,
            finalMatrix,
            0
        )

        GLES20.glUniform4fv(
            colorHandle,
            1,
            color,
            0
        )

        indexBuffer.position(0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )
    }

    fun handleTouch(
        x: Float,
        y: Float,
        pressed: Boolean
    ) {
        logic.handleTouch(x, y, pressed)
    }

    fun pauseGame() {
        logic.pause()
    }

    private fun createProgram(
        vertexSource: String,
        fragmentSource: String
    ): Int {
        val vertex = loadShader(
            GLES20.GL_VERTEX_SHADER,
            vertexSource
        )

        val fragment = loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            fragmentSource
        )

        return GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertex)
            GLES20.glAttachShader(it, fragment)
            GLES20.glLinkProgram(it)
        }
    }

    private fun loadShader(
        type: Int,
        source: String
    ): Int {
        return GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, source)
            GLES20.glCompileShader(it)
        }
    }
}
