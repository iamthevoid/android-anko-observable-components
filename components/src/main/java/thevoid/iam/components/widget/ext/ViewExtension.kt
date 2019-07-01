package thevoid.iam.components.widget.ext

import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import iam.thevoid.ae.*
import iam.thevoid.e.mergeWith
import iam.thevoid.e.safe
import iam.thevoid.util.Optional
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import thevoid.iam.components.widget.util.ObserveListener
import thevoid.iam.components.R
import thevoid.iam.components.widget.Setter
import thevoid.iam.components.rx.RxLoading
import thevoid.iam.components.rx.fields.*
import thevoid.iam.components.widget.delegate.OnGestureDelegate

/**
 * BASE
 */

fun <T : Any, V : View> V.addSetter(flowable: Flowable<T>, setter: V.(T) -> Unit = {}) {
    observeListener.apply {
        subscribeSetter(object : Setter<V, T>(this@addSetter, flowable) {
            override fun set(view: V?, component: T) {
                view?.apply { setter(this, component) }
            }
        })
    }
}

fun <T : CharSequence, V : View> V.addGetter(consumer: ((T) -> Unit) -> Unit, rxCharSequence: RxCharSequence<T>) =
    addSetter(Flowable.create<T>({ emitter ->
        consumer { emitter.onNext(it) }
    }, BackpressureStrategy.LATEST)) { rxCharSequence.set(it) }

fun <T : Any, V : View> V.addGetter(consumer: ((T?) -> Unit) -> Unit, rxField: RxField<T>) =
    addSetter(Flowable.create<Optional<T>>({ emitter ->
        consumer { emitter.onNext(Optional.of(it)) }
    }, BackpressureStrategy.LATEST)) { rxField.set(it.elem) }

fun <T : Any, V : View> V.addGetter(consumer: ((T) -> Unit) -> Unit, rxField: RxItem<T>) =
    addSetter(Flowable.create<T>({ emitter ->
        consumer { emitter.onNext(it) }
    }, BackpressureStrategy.LATEST)) { rxField.set(it) }

fun <V : View> V.addGetter(consumer: ((Int) -> Unit) -> Unit, rxInt: RxInt) =
    addSetter(Flowable.create<Int>({ emitter ->
        consumer { emitter.onNext(it) }
    }, BackpressureStrategy.LATEST)) { rxInt.set(it) }

fun <V : View> V.addGetter(consumer: ((Long) -> Unit) -> Unit, rxLong: RxLong) =
    addSetter(Flowable.create<Long>({ emitter ->
        consumer { emitter.onNext(it) }
    }, BackpressureStrategy.LATEST)) { rxLong.set(it) }

fun <V : View> V.addGetter(consumer: ((Boolean) -> Unit) -> Unit, rxBoolean: RxBoolean) =
    addSetter(Flowable.create<Boolean>({ emitter ->
        consumer { emitter.onNext(it) }
    }, BackpressureStrategy.LATEST)) { rxBoolean.set(it) }

fun <V : View> V.addGetter(consumer: ((Double) -> Unit) -> Unit, rxDouble: RxDouble) =
    addSetter(Flowable.create<Double>({ emitter ->
        consumer { emitter.onNext(it) }
    }, BackpressureStrategy.LATEST)) { rxDouble.set(it) }

fun <V : View> V.addGetter(consumer: ((Float) -> Unit) -> Unit, rxFloat: RxFloat) =
    addSetter(Flowable.create<Float>({ emitter ->
        consumer { emitter.onNext(it) }
    }, BackpressureStrategy.LATEST)) { rxFloat.set(it) }

private val View.observeListener: ObserveListener
    get() = ((getTag(R.id.listener) as? ObserveListener)
        ?: ObserveListener().also {
            setTag(R.id.listener, it)
            addOnAttachStateChangeListener(it)
        })

/**
 * Visibility
 */

fun View.hideUntilLoaded(loading: RxLoading) =
    addSetter(loading.asFlowable) { hide(it) }

fun View.hideUntilLoaded(loading: RxLoading, vararg loadings: RxLoading) =
    addSetter(Flowable.merge(loading.asFlowable.mergeWith(loadings.map { it.asFlowable }))) { hide(it) }

fun View.hideWhenLoaded(loading: RxLoading) =
    addSetter(loading.asFlowable) { hide(!it) }

fun View.hideWhenLoaded(loading: RxLoading, vararg loadings: RxLoading) =
    addSetter(Flowable.merge(loading.asFlowable.mergeWith(loadings.map { it.asFlowable }))) { hide(!it) }

fun View.goneUntilLoaded(loading: RxLoading) =
    addSetter(loading.asFlowable) { gone(it) }

fun View.goneUntilLoaded(loading: RxLoading, vararg loadings: RxLoading) =
    addSetter(Flowable.merge(loading.asFlowable.mergeWith(loadings.map { it.asFlowable }))) { gone(it) }

fun View.goneWhenLoaded(loading: RxLoading) =
    addSetter(loading.asFlowable) { gone(!it) }

fun View.goneWhenLoaded(loading: RxLoading, vararg loadings: RxLoading) =
    addSetter(Flowable.merge(loading.asFlowable.mergeWith(loadings.map { it.asFlowable }))) { gone(!it) }

fun View.gone(needGone: Flowable<Boolean>) =
    addSetter(needGone) { gone(it) }

fun View.gone(needGone: RxField<Boolean>) =
    addSetter(needGone.observe()) { gone(it.elem.safe) }

fun View.gone(needGone: RxItem<Boolean>) =
    addSetter(needGone.observe()) { gone(it) }

fun View.hide(needHide: Flowable<Boolean>) =
    addSetter(needHide) { hide(it) }

fun View.hide(needHide: RxField<Boolean>) =
    addSetter(needHide.observe()) { hide(it.elem.safe) }

fun View.hide(needHide: RxItem<Boolean>) =
    addSetter(needHide.observe()) { hide(it) }

fun View.setBackgroundColor(color: Flowable<Int>) =
    addSetter(color) { setBackgroundColor(it) }

fun View.setBackgroundColorResource(color: Flowable<Int>) =
    addSetter(color) { setBackgroundColor(color(it)) }

fun View.setBackgroundDrawable(background: Flowable<Drawable>) =
    addSetter(background) { setBackgroundDrawable(it) }

fun View.setBackgroundResource(background: Flowable<Int>) =
    addSetter(background) { setBackgroundResource(it) }

/**
 * Focus
 */


fun View.setFocus(focus: RxBoolean) =
    setFocus(focus.observe())

fun View.setFocus(focus: Flowable<Boolean>) =
    addSetter(focus) { requestFocus ->
        val listener = onFocusChangeListener
        onFocusChangeListener = null

        if (requestFocus)
            requestFocusFromTouch()
        else
            resetFocus()

        onFocusChangeListener = listener
    }

fun View.onFocusChange(onChange: RxItem<Boolean>) =
    onFocusChange(onChange) { it }

fun View.onFocusChange(onChange: RxField<Boolean>) =
    onFocusChange(onChange) { it }

fun <T : Any> View.onFocusChange(onChange: RxField<T>, mapper: (Boolean) -> T?) =
    addGetter({
        onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            it.invoke(mapper(hasFocus))
        }
    }, onChange)

fun <T : Any> View.onFocusChange(onChange: RxItem<T>, mapper: (Boolean) -> T) =
    addGetter({
        onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            it.invoke(mapper(hasFocus))
        }
    }, onChange)

/**
 * Gesture
 */

private val View.gestureDetector
    get() = (getTag(R.id.gestureDetector) as? GestureDetector) ?: GestureDetector(context, gestureDetectorCallback)

private val View.gestureDetectorCallback: OnGestureDelegate
    get() = (getTag(R.id.gestureDetectorCallback) as? OnGestureDelegate)
        ?: OnGestureDelegate().also { setTag(R.id.gestureDetectorCallback, it) }

/**
 * ON SCROLL
 */

fun View.onScroll(rxOnScroll: RxField<OnScroll>) = onScroll(rxOnScroll) { it }

fun <T : Any> View.onScroll(rxOnScroll: RxField<T>, mapper: (OnScroll) -> T) =
    addGetter({
        gestureDetectorCallback.addOnScrollCallback(object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                it.invoke(mapper(OnScroll(e1, e2, distanceX, distanceY)))
                return true
            }
        })
        setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }, rxOnScroll).also { setClickable() }


/**
 * ON FLING
 */


fun View.onFling(rxOnFling: RxField<OnFling>) = onFling(rxOnFling) { it }

fun <T : Any> View.onFling(rxOnFling: RxField<T>, mapper: (OnFling) -> T) =
    addGetter({
        gestureDetectorCallback.addOnFlingCallback(object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                it.invoke(mapper(OnFling(e1, e2, velocityX, velocityY)))
                return true
            }
        })
        setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }, rxOnFling).also { setClickable() }


/**
 * ON DOWN
 */


fun View.onDown(rxOnDown: RxField<Optional<MotionEvent>>) = onDown(rxOnDown) { it }

fun <T : Any> View.onDown(rxOnDown: RxField<T>, mapper: (Optional<MotionEvent>) -> T) =
    addGetter({
        gestureDetectorCallback.addOnDownCallback(object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                it.invoke(mapper(Optional.of(e)))
                return true
            }
        })
        setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }, rxOnDown).also { setClickable() }

/**
 * ON SINGLE TAP UP
 */

fun View.onSingleTapUp(rxOnSingleTapUp: RxField<Optional<MotionEvent>>) = onSingleTapUp(rxOnSingleTapUp) { it }

fun <T : Any> View.onSingleTapUp(rxOnSingleTapUp: RxField<T>, mapper: (Optional<MotionEvent>) -> T) =
    addGetter({
        gestureDetectorCallback.addOnSingleTapUpCallback(object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                it.invoke(mapper(Optional.of(e)))
                return true
            }
        })
        setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }, rxOnSingleTapUp).also { setClickable() }

/**
 * ON SHOW PRESS
 */

fun View.onShowPress(rxOnShowPress: RxField<Optional<MotionEvent>>) = onShowPress(rxOnShowPress) { it }

fun <T : Any> View.onShowPress(rxOnShowPress: RxField<T>, mapper: (Optional<MotionEvent>) -> T) =
    addGetter({
        gestureDetectorCallback.addOnShowPressCallback(object : GestureDetector.SimpleOnGestureListener() {
            override fun onShowPress(e: MotionEvent?) = it.invoke(mapper(Optional.of(e)))
        })
        setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }, rxOnShowPress).also { setClickable() }

/**
 * ON LONG PRESS
 */

fun View.onLongPress(rxOnLongPress: RxField<Optional<MotionEvent>>) = onLongPress(rxOnLongPress) { it }

fun <T : Any> View.onLongPress(rxOnLongPress: RxField<T>, mapper: (Optional<MotionEvent>) -> T) =
    addGetter({
        gestureDetectorCallback.addOnLongPressCallback(object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent?) = it.invoke(mapper(Optional.of(e)))
        })
        setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }, rxOnLongPress).also { setClickable() }

data class OnScroll(val e1: MotionEvent?, val e2: MotionEvent?, val distanceX: Float, val distanceY: Float)

data class OnFling(val e1: MotionEvent?, val e2: MotionEvent?, val velocityX: Float, val velocityY: Float)