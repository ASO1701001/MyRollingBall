package jp.ac.asojuku.st.myrollingball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener, SurfaceHolder.Callback {

    private var surfaceWidth: Int = 0
    private var surfaceHeight: Int = 0

    private val radius = 50.0f
    private val coef = 1000.0f

    private var ballX: Float = 0f
    private var ballY: Float = 0f
    private var vx: Float = 0f
    private var vy: Float = 0f
    private var time: Long = 0L

    // 障害物座標
    // 01
    private val block01_x = 500f
    private val block01_y = 600f
    private val block01_radius = 50.0f
    // 02
    private val block02_top = 300f
    private val block02_bottom = 350f
    private val block02_left = 800f
    private val block02_right = 200f

    // ゴール座標
    private val block03_top = 1000f
    private val block03_bottom = 1100f
    private val block03_left = 200f
    private val block03_right = 1000f

    private var drawFlg: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val holder = surfaceView.holder
        holder.addCallback(this)

        reset.setOnClickListener {
            ballX = 675.toFloat()
            ballY = 853.toFloat()
            vx = 0f
            vy = 0f

            drawCanvas()

            icon_text.setText(R.string.v_start_text)

            drawFlg = true
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (time == 0L) time = System.currentTimeMillis()
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if (!drawFlg) return
            val x = event.values[0] * -1
            val y = event.values[1]

            var t = (System.currentTimeMillis() - time).toFloat()
            time = System.currentTimeMillis()
            t /= 1000.0f

            val dx = (vx * t) + (x * t * t) / 2.0f
            val dy = (vy * t) + (y * t * t) / 2.0f
            ballX += (dx * coef)
            ballY += (dy * coef)
            vx += (x * t)
            vy += (y * t)

            if ((ballX - radius) < 0 && vx < 0) {
                vx = -vx / 1.5f
                ballX = radius
            } else if ((ballX + radius) > surfaceWidth && vx > 0) {
                vx = -vx / 1.5f
                ballX = surfaceWidth - radius
            }
            if ((ballY - radius) < 0 && vy < 0) {
                vy = -vy / 1.5f
                ballY = radius
            } else if ((ballY + radius) > surfaceHeight && vy > 0) {
                vy = -vy / 1.5f
                ballY = surfaceHeight - radius
            }

            // 障害物01
            if ((ballY < (block01_y + block01_radius) && block01_y < (ballY + radius)) && (ballX < (block01_x + block01_radius) && block01_x < (ballX + radius))) {
                icon_text.setText(R.string.v_go_text)
                drawFlg = false
            }
            // 障害物02
            if (ballY < block02_bottom + radius && ballY > block02_top && ballX > block02_right && ballX < block02_left) {
                icon_text.setText(R.string.v_go_text)
                drawFlg = false
            }

            // ゴール
            if ((ballY < block03_bottom && block03_top < (ballY + radius)) && (ballX < block03_right && block03_left < (ballX + radius))) {
                icon_text.setText(R.string.v_su_text)
                drawFlg = false
            }

            if (drawFlg) this.drawCanvas()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        // サーフェスの幅と高さをプロパティに保存しておく
        surfaceWidth = width
        surfaceHeight = height

        // ボールの初期値を保存しておく
        ballX = (width / 2).toFloat()
        ballY = (height / 2).toFloat()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(
            this,
            accSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    private fun drawCanvas() {
        val canvas = surfaceView.holder.lockCanvas()
        canvas.drawColor(Color.WHITE)

        // 障害物01
        canvas.drawCircle(
            block01_x,
            block01_y,
            block01_radius,
            Paint().apply {
                color = Color.RED
            }
        )

        // 障害物02
        canvas.drawRect(
            block02_left,
            block02_top,
            block02_right,
            block02_bottom,
            Paint().apply {
                color = Color.RED
            }
        )

        // ゴール01
        canvas.drawRect(
            block03_left,
            block03_top,
            block03_right,
            block03_bottom,
            Paint().apply {
                color = Color.BLUE
            }
        )

        // 自機
        canvas.drawCircle(
            ballX,
            ballY,
            radius,
            Paint().apply {
                color = Color.BLACK
            }
        )

        surfaceView.holder.unlockCanvasAndPost(canvas)
    }
}
