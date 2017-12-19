package com.example.chris.circlemenu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.support.annotation.FloatRange
import android.support.annotation.Keep
import android.util.AttributeSet
import android.view.View
import android.util.Log

/**
 * Created by Chris on 12/15/17.
 */
class RingEffectView: View {
    private val paint: Paint by lazy { initPaint() }
    private val path: Path by lazy { Path() }

    private var angle = 0f
    private var startAngle = 0f
    private var radius = 0

    constructor(context: Context): this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet)

    private fun initPaint(): Paint {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        return paint
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!path.isEmpty) {
            canvas.save()
            canvas.translate(width /  2f, height / 2f)
            canvas.drawPath(path, paint)
            canvas.restore()
        }
    }

    override fun setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        paint.alpha = (255 * alpha).toInt()
        invalidate()
    }

    override fun getAlpha(): Float {
        return paint.alpha / 255f
    }

    fun setStartAngle(@FloatRange(from = 0.0, to = 360.0) startAngle: Float) {
        this.startAngle = startAngle
        angle = 0f

        val sw = paint.strokeWidth * 0.5f
        val radius = radius - sw

        path.reset()
        val x = Math.cos(Math.toRadians(startAngle.toDouble())) * radius
        val y = Math.sin(Math.toRadians(startAngle.toDouble())) * radius
        path.moveTo(x.toFloat(), y.toFloat())
    }

    fun setStrokeColor(color: Int) {
        paint.color = color
    }

    fun setStrokeWidth(width: Int) {
        paint.strokeWidth = width.toFloat()
    }

    fun setRadius(radius: Int) {
        this.radius = radius
    }

    @Keep
    fun setAngle(@FloatRange(from = 0.0, to = 360.0) animAngle: Float) {
        val diff = animAngle - angle
        val stepCount = diff / STEP_DEGREE
        val stepMod = diff % STEP_DEGREE

        val sw = paint.strokeWidth * .5f
        val rad = radius - sw

        Log.d("Anim angle: ", animAngle.toString())

        //draw more lines if the animation angle is more than STEP_DEGREE
        (1..stepCount.toInt()).map {
            val stepAngle = startAngle + angle + STEP_DEGREE * it
            val x = Math.cos(Math.toRadians(stepAngle.toDouble())) * rad
            val y = Math.sin(Math.toRadians(stepAngle.toDouble())) * rad
            path.lineTo(x.toFloat(), y.toFloat())
        }

        val stepAngle = startAngle + angle + STEP_DEGREE * stepCount + stepMod
        val x = Math.cos(Math.toRadians(stepAngle.toDouble())) * rad
        val y = Math.sin(Math.toRadians(stepAngle.toDouble())) * rad
        path.lineTo(x.toFloat(), y.toFloat())

        angle = animAngle

        invalidate()
    }

    companion object {
        private const val STEP_DEGREE = 5
    }
}