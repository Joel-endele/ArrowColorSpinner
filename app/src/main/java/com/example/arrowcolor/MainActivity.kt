package com.example.arrowcolor

import com.example.arrowcolor.R
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.atan2

class MainActivity : AppCompatActivity() {
    private fun setupPaletteDot(view: View, color: Int) {
        val bg = view.background.mutate() as android.graphics.drawable.GradientDrawable
        bg.setColor(color)

        view.setOnClickListener {
            if (started || usedColors.contains(color)) return@setOnClickListener

            selectedColor = color

            // alle normal
            listOf(
                R.id.red, R.id.green, R.id.blue, R.id.yellow,
                R.id.purple, R.id.gray, R.id.white
            ).forEach {
                findViewById<View>(it).scaleX = 1f
                findViewById<View>(it).scaleY = 1f
            }

            // ausgewählter größer
            view.animate().scaleX(1.3f).scaleY(1.3f).setDuration(150).start()
        }
    }


    data class ColorPoint(
        val x: Float,
        val y: Float,
        val color: Int,
        val view: View,
        var angle: Float = 0f
    )

    private val points = mutableListOf<ColorPoint>()
    private val usedColors = mutableSetOf<Int>()

    private var selectedColor = Color.RED
    private var started = false
    private var index = 0
    private var currentRotation = 0f

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.root)
        val arrow = findViewById<View>(R.id.arrow)
        val palette = findViewById<View>(R.id.palette)
        val startBtn = findViewById<Button>(R.id.start)
        val resetBtn = findViewById<Button>(R.id.reset)

        root.setOnTouchListener { _, event ->
            if (!started &&
                event.action == MotionEvent.ACTION_DOWN &&
                event.y < root.height * 0.85 &&
                !usedColors.contains(selectedColor)
            ) {
                val dot = View(this).apply {
                    setBackgroundResource(R.drawable.dot)

                    val bg = background.mutate() as android.graphics.drawable.GradientDrawable
                    bg.setColor(selectedColor)

                    layoutParams = android.widget.FrameLayout.LayoutParams(32, 32)
                    x = event.x - 16
                    y = event.y - 16
                }

                (root as android.widget.FrameLayout).addView(dot)

                points.add(ColorPoint(event.x, event.y, selectedColor, dot))
                usedColors.add(selectedColor)

                val paletteView = when (selectedColor) {
                    Color.RED -> R.id.red
                    Color.GREEN -> R.id.green
                    Color.BLUE -> R.id.blue
                    Color.YELLOW -> R.id.yellow
                    Color.MAGENTA -> R.id.purple
                    Color.GRAY -> R.id.gray
                    Color.WHITE -> R.id.white
                    else -> null
                }

                paletteView?.let {
                    val v = findViewById<View>(it)
                    val bg = v.background.mutate() as android.graphics.drawable.GradientDrawable
                    bg.setColor(Color.LTGRAY)
                    v.alpha = 0.4f
                    v.scaleX = 1f
                    v.scaleY = 1f
                }

            }
            true
        }

        startBtn.setOnClickListener {
            if (points.isEmpty()) return@setOnClickListener
            started = true
            palette.visibility = View.GONE
            startBtn.visibility = View.GONE
            sortClockwise(root)
            index = 0
            step(root, arrow)
        }

        arrow.setOnClickListener {
            if (started) step(root, arrow)
        }

        resetBtn.setOnClickListener {
            listOf(
                Pair(R.id.red, Color.RED),
                Pair(R.id.green, Color.GREEN),
                Pair(R.id.blue, Color.BLUE),
                Pair(R.id.yellow, Color.YELLOW),
                Pair(R.id.purple, Color.MAGENTA),
                Pair(R.id.gray, Color.GRAY),
                Pair(R.id.white, Color.WHITE)
            ).forEach { (id, color) ->
                val v = findViewById<View>(id)
                val bg = v.background.mutate() as android.graphics.drawable.GradientDrawable
                bg.setColor(color)
                v.alpha = 1f
                v.scaleX = 1f
                v.scaleY = 1f
            }

            points.forEach { (root as android.widget.FrameLayout).removeView(it.view) }
            points.clear()
            usedColors.clear()
            root.setBackgroundColor(Color.DKGRAY)
            palette.visibility = View.VISIBLE
            startBtn.visibility = View.VISIBLE
            started = false
            index = 0
            currentRotation = 0f
            arrow.rotation = 0f
        }

        setupPaletteDot(findViewById(R.id.red), Color.RED)
        setupPaletteDot(findViewById(R.id.green), Color.GREEN)
        setupPaletteDot(findViewById(R.id.blue), Color.BLUE)
        setupPaletteDot(findViewById(R.id.yellow), Color.YELLOW)
        setupPaletteDot(findViewById(R.id.purple), Color.MAGENTA)
        setupPaletteDot(findViewById(R.id.gray), Color.GRAY)
        setupPaletteDot(findViewById(R.id.white), Color.WHITE)

    }

    private fun sortClockwise(root: View) {
        val cx = root.width / 2f
        val cy = root.height / 2f

        points.forEach {
            it.angle = ((Math.toDegrees(
                atan2(it.y - cy, it.x - cx).toDouble()
            ).toFloat()) + 360) % 360
        }

        points.sortBy { it.angle }
    }

    private fun step(root: View, arrow: View) {
        val p = points[index % points.size]
        root.setBackgroundColor(p.color)

        val cx = root.width / 2f
        val cy = root.height / 2f
        var target =
            Math.toDegrees(atan2(p.y - cy, p.x - cx).toDouble()).toFloat() + 90

        while (target < currentRotation) target += 360

        ObjectAnimator.ofFloat(
            arrow,
            View.ROTATION,
            currentRotation,
            target
        ).apply {
            duration = 250
            start()
        }

        currentRotation = target
        index++
    }
}
