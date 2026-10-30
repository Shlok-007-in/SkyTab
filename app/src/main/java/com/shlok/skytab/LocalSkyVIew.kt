
package com.shlok.skytab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlin.math.hypot
import android.util.AttributeSet

class LocalSkyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Star(
        val name: String,
        val ra: Float,
        val dec: Float,
        val magnitude: Float
    )

    private val stars = listOf(
        Star("Polaris", 2.53f, 89.26f, 2.0f),
        Star("Dubhe", 11.06f, 61.75f, 1.8f),
        Star("Merak", 11.03f, 56.38f, 2.4f),
        Star("Phecda", 11.90f, 53.69f, 2.4f),
        Star("Megrez", 12.26f, 57.03f, 3.3f),
        Star("Alioth", 12.90f, 55.96f, 1.8f),
        Star("Mizar", 13.40f, 54.93f, 2.2f),
        Star("Alkaid", 13.79f, 49.31f, 1.9f),
        Star("Betelgeuse", 5.92f, 7.41f, 0.5f),
        Star("Rigel", 5.24f, -8.20f, 0.1f),
        Star("Bellatrix", 5.42f, 6.35f, 1.6f),
        Star("Mintaka", 5.53f, -0.30f, 2.2f),
        Star("Alnilam", 5.60f, -1.20f, 1.7f),
        Star("Alnitak", 5.68f, -1.94f, 1.8f),
        Star("Saiph", 5.80f, -9.67f, 2.1f),
        Star("Sirius", 6.75f, -16.72f, -1.5f),
        Star("Procyon", 7.66f, 5.22f, 0.4f),
        Star("Aldebaran", 4.60f, 16.51f, 0.9f),
        Star("Vega", 18.62f, 38.78f, 0.0f),
        Star("Deneb", 20.69f, 45.28f, 1.3f),
        Star("Altair", 19.85f, 8.87f, 0.8f),
        Star("Arcturus", 14.26f, 19.18f, 0.0f),
        Star("Spica", 13.42f, -11.16f, 1.0f),
        Star("Antares", 16.49f, -26.43f, 1.1f),
        Star("Regulus", 10.14f, 11.97f, 1.4f),
        Star("Capella", 5.28f, 46.00f, 0.1f),
        Star("Canopus", 6.40f, -52.70f, -0.7f),
        Star("Castor", 7.58f, 31.89f, 1.6f),
        Star("Pollux", 7.76f, 28.03f, 1.1f),
        Star("Fomalhaut", 22.96f, -29.62f, 1.2f)
    )

    private val constellations = listOf(
        listOf("Dubhe", "Merak", "Phecda", "Megrez",
            "Alioth", "Mizar", "Alkaid"),
        listOf("Betelgeuse", "Bellatrix", "Mintaka",
            "Alnilam", "Alnitak", "Saiph", "Rigel"),
        listOf("Vega", "Deneb", "Altair")
    )

    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(90, 170, 220)
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(210, 225, 255)
        textSize = 25f
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(45, 110, 180, 230)
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private var zoom = 1f
    private var panX = 0f
    private var panY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var moved = false
    private var downX = 0f
    private var downY = 0f
    private var pinchDistance = 0f
    private var pinchZoom = 1f

    init {
        setBackgroundColor(Color.rgb(4, 8, 24))
        isClickable = true
    }

    private fun starX(star: Star) = star.ra / 24f * width
    private fun starY(star: Star) = (90f - star.dec) / 180f * height

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.translate(panX, panY)
        canvas.scale(zoom, zoom, width / 2f, height / 2f)

        // Celestial-coordinate grid: right ascension and declination.
        for (ra in 0..24 step 2) {
            val x = ra / 24f * width
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
        }
        for (dec in -60..80 step 20) {
            val y = (90f - dec) / 180f * height
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }

        // Constellation guide lines.
        val byName = stars.associateBy { it.name }
        for (group in constellations) {
            val path = Path()
            group.mapNotNull { byName[it] }.forEachIndexed { index, star ->
                val x = starX(star)
                val y = starY(star)
                if (index == 0) path.moveTo(x, y)
                else path.lineTo(x, y)
            }
            canvas.drawPath(path, linePaint)
        }

        // Stars and their names.
        for (star in stars) {
            val x = starX(star)
            val y = starY(star)
            val radius = (4.5f - star.magnitude.coerceIn(-1.5f, 4f) * 0.65f)
                .coerceIn(2f, 6f)

            canvas.drawCircle(x, y, radius, starPaint)

            if (star.magnitude <= 1.5f) {
                canvas.drawText(star.name, x + 9f, y - 8f, textPaint)
            }
        }

        canvas.restore()

        textPaint.textSize = 27f
        canvas.drawText("OFFLINE STAR CHART", 24f, 40f, textPaint)
        textPaint.textSize = 20f
        canvas.drawText("Drag to explore • Pinch to zoom • Tap a star",
            24f, height - 24f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                lastX = event.x
                lastY = event.y
                moved = false
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2) {
                    pinchDistance = distance(event)
                    pinchZoom = zoom
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2) {
                    val distance = distance(event)
                    if (pinchDistance > 0f) {
                        zoom = (pinchZoom * distance / pinchDistance)
                            .coerceIn(1f, 5f)
                    }
                    moved = true
                } else {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    panX += dx
                    panY += dy
                    if (hypot(event.x - downX, event.y - downY) > 12f) {
                        moved = true
                    }
                }
                lastX = event.x
                lastY = event.y
                invalidate()
                return true
            }

            MotionEvent.ACTION_POINTER_UP -> {
                pinchDistance = 0f
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!moved) selectStar(event.x, event.y)
                performClick()
                return true
            }
        }
        return true
    }

    private fun distance(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return hypot(dx, dy)
    }

    private fun selectStar(touchX: Float, touchY: Float) {
        val mapX = (touchX - panX - width / 2f) / zoom + width / 2f
        val mapY = (touchY - panY - height / 2f) / zoom + height / 2f

        val selected = stars.minByOrNull { star ->
            hypot(starX(star) - mapX, starY(star) - mapY)
        }

        if (selected != null) {
            val distance = hypot(starX(selected) - mapX, starY(selected) - mapY)
            if (distance < 35f) {
                Toast.makeText(
                    context,
                    "${selected.name}  •  RA ${selected.ra}h  •  Dec ${selected.dec}°",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
