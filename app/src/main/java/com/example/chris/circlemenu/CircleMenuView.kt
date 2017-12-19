package com.example.chris.circlemenu

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.support.annotation.IntegerRes
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout

/**
 * Created by Chris on 12/15/17.
 */
class CircleMenuView: FrameLayout, View.OnClickListener {
    private val buttons = mutableListOf<View>()
    private val buttonRect = Rect()

    private lateinit var menuButton: FloatingActionButton
    private lateinit var ringView: RingEffectView

    private var closedState = true
    private var isAnimating = false

    private var iconMenu = 0
    private var iconClose = 0
    private var durationRing = 0
    private var durationOpen = 0
    private var durationClose = 0
    private var desiredSize = 0
    private var ringRadius = 0

    private var distance = 0f

    private var eventListener: MenuEventListener? = null

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        if (attrs == null) throw IllegalArgumentException("Not buttons icons or colors set")

        var menuButtonColor: Int

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CircleMenuView, 0, 0)

        val iconArrayId = a.getResourceId(R.styleable.CircleMenuView_button_icons, 0)
        val colorArrayId = a.getResourceId(R.styleable.CircleMenuView_button_colors, 0)

        val iconsIds = resources.obtainTypedArray(iconArrayId)
        val colorsIds = resources.getIntArray(colorArrayId)

        val buttonCount = Math.min(iconsIds.length(), colorsIds.size)

        val icons = mutableListOf<Int>()
        val colors = mutableListOf<Int>()
        (0 until buttonCount).map {
            icons.add(iconsIds.getResourceId(it, -1))
            colors.add(colorsIds[it])
        }

        iconsIds.recycle()

        iconMenu = a.getResourceId(R.styleable.CircleMenuView_icon_menu, R.drawable.ic_menu_black)
        iconClose = a.getResourceId(R.styleable.CircleMenuView_icon_close, R.drawable.ic_close_black)

        durationRing = a.getInteger(R.styleable.CircleMenuView_duration_ring, resources.getInteger(android.R.integer.config_mediumAnimTime))
        durationOpen = a.getInteger(R.styleable.CircleMenuView_duration_open, resources.getInteger(android.R.integer.config_mediumAnimTime))
        durationClose = a.getInteger(R.styleable.CircleMenuView_duration_close, resources.getInteger(android.R.integer.config_mediumAnimTime))

        val density = context.resources.displayMetrics.density
        val defaultDistance = DEFAULT_DISTANCE * density
        distance = a.getDimension(R.styleable.CircleMenuView_distance, defaultDistance)

        menuButtonColor = a.getColor(R.styleable.CircleMenuView_icon_color, Color.WHITE)

        a.recycle()

        initLayout(context)
        initMenu(menuButtonColor)
        initButtons(context, icons, colors)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val w = View.resolveSizeAndState(desiredSize, widthMeasureSpec, 0)
        val h = View.resolveSizeAndState(desiredSize, heightMeasureSpec, 0)

        setMeasuredDimension(w, h)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (!changed && isAnimating) {
            return
        }

        menuButton.getContentRect(buttonRect)

        ringView.setStrokeWidth(buttonRect.width())
        ringView.setRadius(ringRadius)

        val lp = ringView.layoutParams
        lp.width = right - left
        lp.height = bottom - top
        ringView.layoutParams = lp
    }

    override fun onClick(p0: View?) {
        if (isAnimating) {
            return
        }

        val click = getButtonClickAnimation(p0 as FloatingActionButton)
        click.duration = durationRing.toLong()
        click.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                eventListener?.onButtonClickAnimationStart(this@CircleMenuView, buttons.indexOf(p0))
            }

            override fun onAnimationEnd(animation: Animator?) {
                closedState = true
                eventListener?.onButtonClickAnimationEnd(this@CircleMenuView, buttons.indexOf(p0))
            }
        })

        click.start()
    }

    private fun initLayout(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.circle_menu, this, true)

        setWillNotDraw(true)
        clipChildren = false
        clipToPadding = false

        val density = context.resources.displayMetrics.density
        val buttonSize = DEFAULT_BUTTON_SIZE * density

        ringRadius = (buttonSize + (distance - buttonSize / 2)).toInt()
        desiredSize = (ringRadius * 2 * DEFAULT_RING_SCALE_RATIO).toInt()

        ringView = findViewById(R.id.ring_view)
    }

    private fun initMenu(menuButtonColor: Int) {
        val animListener = object: AnimatorListenerAdapter() {
            override fun onAnimationStart(p0: Animator?) {
                if (closedState) {
                    eventListener?.onMenuOpenAnimationStart(this@CircleMenuView)
                } else {
                    eventListener?.onMenuCloseAnimationStart(this@CircleMenuView)
                }
            }

            override fun onAnimationEnd(p0: Animator?) {
                if (closedState) {
                    eventListener?.onMenuOpenAnimationEnd(this@CircleMenuView)
                } else {
                    eventListener?.onMenuCloseAnimationEnd(this@CircleMenuView)
                }

                closedState = !closedState
            }
        }

        menuButton = findViewById(R.id.circle_menu_main_button)
        menuButton.apply {
            setImageResource(iconMenu)
            backgroundTintList = ColorStateList.valueOf(menuButtonColor)
            setOnClickListener {
                if (isAnimating) {
                    return@setOnClickListener
                }

                val animator = if (closedState) getOpenMenuAnimation() else getCloseMenuAnimation()
                animator.duration = if (closedState) durationClose.toLong() else durationOpen.toLong()
                animator.addListener(animListener)
                animator.start()
            }
        }
    }

    private fun initButtons(context: Context, icons: List<Int>, colors: List<Int>) {
        val buttonsCount = Math.min(icons.size, colors.size)
        (0 until buttonsCount).map {
            val button = FloatingActionButton(context)
            button.apply {
                setImageResource(icons[it])
                backgroundTintList = ColorStateList.valueOf(colors[it])
                isClickable = true
                setOnClickListener(this@CircleMenuView)
                scaleX = 0f
                scaleY = 0f
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                addView(this)
            }

            buttons.add(button)
        }
    }

    private fun getOpenMenuAnimation(): Animator {
        val alphaAnimation = ObjectAnimator.ofFloat(menuButton, "alpha", DEFAULT_CLOSE_ICON_ALPHA)
        val kf0 = Keyframe.ofFloat(0f, 0f)
        val kf1 = Keyframe.ofFloat(0.5f, 60f)
        val kf2 = Keyframe.ofFloat(1f, 0f)

        val pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2)
        val rotateAnim = ObjectAnimator.ofPropertyValuesHolder(menuButton, pvhRotation)
        rotateAnim.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            var iconChanged = false
            override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
                val fraction = valueAnimator!!.animatedFraction
                if (fraction >= 0.5f && !iconChanged) {
                    iconChanged = true
                    menuButton.setImageResource(iconClose)
                }
            }
        })

        val centerX = menuButton.x
        val centerY = menuButton.y

        val buttonsCount = buttons.size
        val angleStep = 360f / buttonsCount

        val buttonsAppear = ValueAnimator.ofFloat(0f, distance)
        buttonsAppear.interpolator = OvershootInterpolator()
        buttonsAppear.addUpdateListener {
            for (view in buttons) {
                view.visibility = View.VISIBLE
            }
        }
        buttonsAppear.addUpdateListener {
            val fraction = it.animatedFraction
            val value = it.animatedValue as Float
            offsetAndScaleButtons(centerX, centerY, angleStep, value, fraction)
        }

        val result = AnimatorSet()
        result.playTogether(alphaAnimation, rotateAnim, buttonsAppear)
        result.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isAnimating = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                isAnimating = false
            }
        })

        return result
    }

    private fun getCloseMenuAnimation(): Animator {
        val scaleX1 = ObjectAnimator.ofFloat(menuButton, "scaleX", 0f)
        val scaleY1 = ObjectAnimator.ofFloat(menuButton, "scaleY", 0f)
        val alpha1 = ObjectAnimator.ofFloat(menuButton, "alpha", 0f)
        val set1 = AnimatorSet()
        set1.playTogether(scaleX1, scaleY1, alpha1)
        set1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                buttons.forEach { it.visibility = View.INVISIBLE }
            }

            override fun onAnimationEnd(animation: Animator?) {
                menuButton.rotation = 60f
                menuButton.setImageResource(iconMenu)
            }
        })

        val angle = ObjectAnimator.ofFloat(menuButton, "rotation", 0f)
        val alpha2 = ObjectAnimator.ofFloat(menuButton, "alpha", 1f)
        val scaleX2 = ObjectAnimator.ofFloat(menuButton, "scaleX", 1f)
        val scaleY2 = ObjectAnimator.ofFloat(menuButton, "scaleY", 1f)
        val set2 = AnimatorSet()
        set2.interpolator = OvershootInterpolator()
        set2.playTogether(angle, alpha2, scaleX2, scaleY2)

        val result = AnimatorSet()
        result.play(set1).before(set2)
        result.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isAnimating = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                isAnimating = false
            }
        })

        return result
    }

    private fun offsetAndScaleButtons(centerX: Float, centerY: Float, angleStep: Float, offset: Float, scale: Float) {
        (0 until buttons.size).map {
            val angle = (angleStep * it - 90).toDouble()
            val x = (Math.cos(Math.toRadians(angle)) * offset)
            val y = (Math.sin(Math.toRadians(angle)) * offset)

            val button = buttons[it]
            button.x = centerX + x.toFloat()
            button.y = centerY + y.toFloat()
            button.scaleX = 1.0f * scale
            button.scaleY = 1.0f * scale
        }
    }

    private fun getButtonClickAnimation(button: FloatingActionButton): Animator {
        val buttonNumber = buttons.indexOf(button) + 1
        val stepAngle = 360f / buttons.size
        val rOStartAngle = (270 - stepAngle + stepAngle * buttonNumber)
        val rStartAngle = rOStartAngle % 360

        val x = Math.cos(Math.toRadians(rStartAngle.toDouble())) * distance
        val y = Math.sin(Math.toRadians(rStartAngle.toDouble())) * distance

        val pivotX = button.pivotX
        val pivotY = button.pivotY
        button.pivotX = pivotX - x.toFloat()
        button.pivotY = pivotY - y.toFloat()

        val rotateButton = ObjectAnimator.ofFloat(button, "rotation", 0f, 360f)
        rotateButton.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                button.pivotX = pivotX
                button.pivotY = pivotY
            }
        })

        val elevation = menuButton.compatElevation

        ringView.visibility = View.INVISIBLE
        ringView.setStartAngle(rStartAngle)

        val csl = button.backgroundTintList
        if (csl != null) {
            ringView.setStrokeColor(csl.defaultColor)
        }

        val ring = ObjectAnimator.ofFloat(ringView, "angle", 360f) //Calls setAngle() from RingEffectView to animate
        val scaleX = ObjectAnimator.ofFloat(ringView, "scaleX", 1f, DEFAULT_RING_SCALE_RATIO)
        val scaleY = ObjectAnimator.ofFloat(ringView, "scaleY", 1f, DEFAULT_RING_SCALE_RATIO)
        val visible = ObjectAnimator.ofFloat(ringView, "alpha", 1f, 0f)

        val lastSet = AnimatorSet()
        lastSet.playTogether(scaleX, scaleY, visible, getCloseMenuAnimation())

        val firstSet = AnimatorSet()
        firstSet.playTogether(rotateButton, ring)

        val result = AnimatorSet()
        result.play(firstSet).before(lastSet)
        result.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isAnimating = true

                button.compatElevation = elevation + 1
                ViewCompat.setZ(ringView, elevation + 1)

                buttons.filter { it != button }.map { (it as FloatingActionButton).compatElevation = 0f }

                ringView.scaleX = 1f
                ringView.scaleY = 1f
                ringView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                isAnimating = false
                buttons.forEach { (it as FloatingActionButton).compatElevation = elevation }
                ViewCompat.setZ(ringView, elevation)
            }
        })

        return result
    }

    companion object {
        const val DEFAULT_BUTTON_SIZE = 56
        const val DEFAULT_DISTANCE = DEFAULT_BUTTON_SIZE * 1.5f
        const val DEFAULT_RING_SCALE_RATIO = 1.3f
        const val DEFAULT_CLOSE_ICON_ALPHA = 0.3f
    }
}