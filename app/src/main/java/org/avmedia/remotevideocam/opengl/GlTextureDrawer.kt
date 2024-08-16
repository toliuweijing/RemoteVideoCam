package org.avmedia.remotevideocam.opengl

import android.opengl.GLES20
import org.webrtc.GlShader
import org.webrtc.GlUtil
import timber.log.Timber

private const val TAG = "GlTextureDrawer"

class GlTextureDrawer {

    private val FULL_RECTANGLE_VERTEX_BUFFER = GlUtil.createFloatBuffer(
        floatArrayOf(
            -1f, -1f, // bottom left
            1f, -1f, // bottom right
            -1f, 1f, // top left
            1f, 1f,  // top right
        )
    )

    private val FULL_RECTANGLE_TEXTURE_BUFFER = GlUtil.createFloatBuffer(
        floatArrayOf(
            0f, 0f, // bottom left
            1f, 0f, // bottom right
            0f, 1f, // top left
            1f, 1f, // top right
        )
    )

    object ShaderSource {
        val POSITION_NAME = "aPosition"
        val TEXCOORD_NAME = "aTexCoord"
        val TEXMATRIX_NAME = "uTexMatrix"
        val VARYING_TEXCOORD_NAME = "vTexCoord"
        val MASK_TEX_NAME = "uMaskTex"
        val EXTERNAL_TEX_NAME = "uExtTex"
        val MASK_OPACITY_NAME = "uOpacity"

        val vertexShader =
            """
            attribute vec4 $POSITION_NAME;
            attribute vec4 $TEXCOORD_NAME;
            uniform mat4 $TEXMATRIX_NAME;
            
            varying vec2 $VARYING_TEXCOORD_NAME;
            
            void main() {
                gl_Position = vec4($POSITION_NAME.x, $POSITION_NAME.y, 1, 1);
                $VARYING_TEXCOORD_NAME = ($TEXMATRIX_NAME * $TEXCOORD_NAME).xy;
            }
        """.trimIndent()

        val fragmentShader =
            """
            #extension GL_OES_EGL_image_external : require
            
            precision mediump float;
            uniform samplerExternalOES $EXTERNAL_TEX_NAME;
            uniform sampler2D $MASK_TEX_NAME;
            uniform float $MASK_OPACITY_NAME;
            
            varying vec2 $VARYING_TEXCOORD_NAME;
            
            void main() {
                vec4 mask_color = texture2D($MASK_TEX_NAME, $VARYING_TEXCOORD_NAME);
                mask_color.a = $MASK_OPACITY_NAME * mask_color.a;
                gl_FragColor = mask_color;
            }
        """.trimIndent()
//        mask_color.a = $MASK_OPACITY_NAME * mask_color.a;
//        mask_color.a = min($MASK_OPACITY_NAME, mask_color.a);
    }

    private var shader : GlShader? = null
    private var positionLocation = 0
    private var texCoordLocation = 0
    private var texTransformLocation = 0
    private var maskTexLocation = 0
    private var externalTexLocation = 0
    private var maskOpacityLocation = 0
    private val active get() = shader != null

    fun init() {
        val shader = GlShader(ShaderSource.vertexShader, ShaderSource.fragmentShader).also {
            this.shader = it
        }
        shader.useProgram()
        positionLocation = shader.getAttribLocation(ShaderSource.POSITION_NAME)
        texCoordLocation = shader.getAttribLocation(ShaderSource.TEXCOORD_NAME)
        texTransformLocation = shader.getUniformLocation(ShaderSource.TEXMATRIX_NAME)
        maskTexLocation = shader.getUniformLocation(ShaderSource.MASK_TEX_NAME)
        maskOpacityLocation = shader.getUniformLocation(ShaderSource.MASK_OPACITY_NAME)

        // Assign texture unit
        GLES20.glUniform1i(maskTexLocation, 1)

        // Assign vertex and texture coordinate attributes
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glVertexAttribPointer(
            positionLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            FULL_RECTANGLE_VERTEX_BUFFER
        )
        GLES20.glEnableVertexAttribArray(texCoordLocation)
        GLES20.glVertexAttribPointer(
            texCoordLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            FULL_RECTANGLE_TEXTURE_BUFFER
        )

        GlUtil.checkNoGLES2Error("$TAG.init")
    }

    fun draw(
        viewportX: Int,
        viewportY: Int,
        viewportWidth: Int,
        viewportHeight: Int,
        textureTransform: FloatArray,
        maskTexId: Int,
        opacity: Float = 1f
    ) {
        if (!active) {
            init()
        }

        shader?.useProgram()

        // bind textures
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, maskTexId)

        GLES20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight)
        GLES20.glUniformMatrix4fv(
            texTransformLocation,
            1,
            false,
            textureTransform,
            0
        )
        Timber.tag(TAG).d("opacity %f", opacity)
        GLES20.glUniform1f(maskOpacityLocation, opacity)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GlUtil.checkNoGLES2Error("$TAG.draw")
    }

    fun release() {
        shader?.release()
        shader = null
    }
}