package com.example.arrowcolor

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.atan2

class MainActivity : AppCompatActivity() {

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

        findViewById<View>(R.id.red).setOnClickListener { selectedColor = Color.RED }
        findViewById<View>(R.id.green).setOnClickListener { selectedColor = Color.GREEN }
        findViewById<View>(R.id.blue).setOnClickListener { selectedColor = Color.BLUE }
        findViewById<View>(R.id.yellow).setOnClickListener { selectedColor = Color.YELLOW }
        findViewById<View>(R.id.purple).setOnClickListener { selectedColor = Color.MAGENTA }
        findViewById<View>(R.id.gray).setOnClickListener { selectedColor = Color.GRAY }
        findViewById<View>(R.id.white).setOnClickListener { selectedColor = Color.WHITE }
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
