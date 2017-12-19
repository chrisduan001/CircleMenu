import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.design.widget.FloatingActionButton
import android.view.ViewPropertyAnimator

/**
 * Created by Chris on 12/19/17.
 */
inline fun Animator.setListener(
        crossinline animationStart: (Animator?) -> Unit = {},
        crossinline animationRepeat: (Animator?) -> Unit = {},
        crossinline animationCancel: (Animator?) -> Unit = {},
        crossinline animationEnd: (Animator?) -> Unit = {}) {

    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            animationStart(animation)
        }

        override fun onAnimationRepeat(animation: Animator?) {
            animationRepeat(animation)
        }

        override fun onAnimationCancel(animation: Animator?) {
            animationCancel(animation)
        }

        override fun onAnimationEnd(animation: Animator?) {
            animationEnd(animation)
        }
    })
}