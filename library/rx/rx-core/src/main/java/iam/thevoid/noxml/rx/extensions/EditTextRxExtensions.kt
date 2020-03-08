package iam.thevoid.noxml.rx.extensions

import android.widget.EditText
import iam.thevoid.ae.*
import iam.thevoid.e.format
import iam.thevoid.noxml.rx.recycler.extensions.setTextResourceSilent
import iam.thevoid.noxml.rx.recycler.extensions.setTextSilent
import io.reactivex.Flowable
import iam.thevoid.noxml.rx.data.fields.*

/**
 * SETTER
 */

fun <T : CharSequence> EditText.setText(text: Flowable<T>, moveCursorToEnd: Boolean = true) =
    addSetter(text) { setTextSilent(it, moveCursorToEnd) }

fun <T : CharSequence> EditText.setText(text: RxCharSequence<T>, moveCursorToEnd: Boolean = true) =
    setText(text.observe(), moveCursorToEnd)

fun EditText.setText(text: RxInt, moveCursorToEnd: Boolean = true) =
    setText(text.observe().map { "$it" }, moveCursorToEnd)

fun EditText.setText(text: RxLong, moveCursorToEnd: Boolean = true) =
    setText(text.observe().map { "$it" }, moveCursorToEnd)

fun EditText.setText(text: RxFloat, precision: Int? = null, moveCursorToEnd: Boolean = true) =
    setText(text.observe().map { it.format(precision) }, moveCursorToEnd)

fun EditText.setText(text: RxDouble, precision: Int? = null, moveCursorToEnd: Boolean = true) =
    setText(text.observe().map { it.format(precision) }, moveCursorToEnd)

fun EditText.setTextResource(textResource: Flowable<Int>, moveCursorToEnd: Boolean) =
    addSetter(textResource) { setTextResourceSilent(it, moveCursorToEnd) }

fun EditText.setTextResource(textResource: RxInt, moveCursorToEnd: Boolean = true) =
    setTextResource(textResource.observe(), moveCursorToEnd)

fun EditText.setTextColor(color: Flowable<Int>) =
    addSetter(color) { setTextColor(it) }

fun EditText.setHintColor(color: Flowable<Int>) =
    addSetter(color) { setHintTextColor(it) }

fun EditText.setTextColorResource(colorResource: Flowable<Int>) =
    addSetter(colorResource) { setTextColor(color(it)) }

fun EditText.setRequestInput(boolean: RxBoolean) =
    setRequestInput(boolean.observe())

fun EditText.setSelection(selection : Flow<Int>) =
    addSetter(selection) { setSelection(it) }

fun EditText.setRequestInput(requestInput: Flowable<Boolean>) =
    addSetter(requestInput) {
        val focusListener = onFocusChangeListener
        onFocusChangeListener = null
        if (it) {
            post {
                requestSoftInput()
                onFocusChangeListener = focusListener
            }
        } else {
            resetFocus()
            onFocusChangeListener = focusListener
        }
    }