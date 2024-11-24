package com.example.opengles

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedFace
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {
    private var surfaceView: GLSurfaceView? = null
    private var session: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceview)
        surfaceView?.apply {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(2)
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            setRenderer(this@MainActivity)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    override fun onResume() {
        super.onResume()
        if (session == null) {
            try {
                session = Session(this)

                // Configure to use the front-facing camera
                val filter =
                    CameraConfigFilter(session).setFacingDirection(CameraConfig.FacingDirection.FRONT)
                val cameraConfig = session!!.getSupportedCameraConfigs(filter)[0]
                session!!.cameraConfig = cameraConfig

                // Configure ARCore session with augmented face mode
                val config = Config(session)
                config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
                session!!.configure(config)
            } catch (e: UnavailableException) {
                Log.e(TAG, "ARCore not available: " + e.message)
                return
            }
        }

        try {
            session!!.resume()
        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "Camera not available: " + e.message)
            session = null
        }

        surfaceView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        surfaceView!!.onPause()
        if (session != null) session!!.pause()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        // initialize variables
        // Called when the surface is first created

        GLES20.glClearColor(
            0.1f,
            0.1f,
            0.1f,
            1.0f
        ) // sets the BG color for the OpenGL rendering surface
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        // when surface change dimensions or orientation

        GLES20.glViewport(
            0,
            0,
            width,
            height
        ) // Updates the OpenGL viewport to match the dimensions of the GLSurfaceView
    }

    override fun onDrawFrame(gl: GL10) {
        // Called to draw the next frame
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT) // Clears the screen for rendering the next frame

        if (session == null) return // Checks if the ARCore session is active before proceeding

        try {
            val frame = session!!.update() // retrieves the latest AR frame from ARCore
            val camera = frame.camera // provides information about the camera's current state

            if (camera.trackingState == TrackingState.TRACKING) { // Indicates whether the AR session can accurately track the environment
                val faces =
                    session!!.getAllTrackables(AugmentedFace::class.java) // Contains data about the geometry and position of a detected face

                faces.forEach { face ->
                    if (face.trackingState == TrackingState.TRACKING) {
                        // Access face mesh data
                        val indices =
                            face.meshTriangleIndices // define how the face's vertices connect to form triangles
                        val facePose = face.centerPose // pose at the center of the face
                        val faceVertices =
                            face.meshVertices // array provides the 3D coordinates of each point on the face
                        val faceNormals =
                            face.meshNormals // provides data for shading and aligning objects

                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during frame update: " + e.message)
        }
    }

    companion object {
        private const val TAG = "GlassesARActivity"
    }
}