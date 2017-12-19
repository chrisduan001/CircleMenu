package com.example.chris.circlemenu.dsl

import android.animation.Animator
import android.view.ViewPropertyAnimator

/**
 * Created by Chris on 12/19/17.
 */

fun Animator.setDslListener(init: AnimListenerHelper.() -> Unit) {
    val listener = AnimListenerHelper()
    listener.init()
    addListener(listener)
}

private typealias AnimListener = (Animator?) -> Unit

class AnimListenerHelper: Animator.AnimatorListener {
    private var animationStart: AnimListener? = null

    fun onAnimationStart(onAnimationStart: AnimListener) {
        animationStart = onAnimationStart
    }

    override fun onAnimationStart(p0: Animator?) {
        animationStart?.invoke(p0)
    }

    private var animationRepeat: AnimListener? = null
    fun onAnimationRepeat(onAnimationRepeat: AnimListener) {
        animationRepeat = onAnimationRepeat
    }

    override fun onAnimationRepeat(p0: Animator?) {
        animationRepeat?.invoke(p0)
    }

    private var animationCancel: AnimListener? = null
    fun onAnimationCancel(onAnimationCancel: AnimListener) {
        animationCancel = onAnimationCancel
    }

    override fun onAnimationCancel(p0: Animator?) {
        animationCancel?.invoke(p0)
    }

    private var animationEnd: AnimListener? = null
    fun onAnimationEnd(onAnimationEnd: AnimListener) {
        animationEnd = onAnimationEnd
    }

    override fun onAnimationEnd(p0: Animator?) {
        animationEnd?.invoke(p0)
    }
}