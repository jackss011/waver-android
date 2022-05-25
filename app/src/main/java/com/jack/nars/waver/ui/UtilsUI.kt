package com.jack.nars.waver.ui

import android.graphics.Rect
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import timber.log.Timber
import kotlin.math.roundToInt


@BindingAdapter("bindUnitaryValue")  // Get value from VM
fun volumeAdapter(bar: Slider, value: Float) {
    if (bar.value != value) {
        bar.value = value
    }
}


@InverseBindingAdapter(attribute = "bindUnitaryValue")  // Set value to VM
fun inverseVolumeAdapter(bar: Slider): Float {
    return bar.value.coerceIn(0f, 1f)
}


@BindingAdapter("app:bindUnitaryValueAttrChanged")
fun setListeners(bar: Slider, attrChange: InverseBindingListener) {
    bar.addOnChangeListener { _, _, _ -> attrChange.onChange() }
}


fun Slider.setupAsIntensity() {
    setLabelFormatter { value: Float -> "${(value * 100).roundToInt()}%" }
}


class SpacingBetween(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val pos = parent.getChildAdapterPosition(view)
        val count = parent.childCount

        if (pos == RecyclerView.NO_POSITION) {
            outRect.set(Rect())
            return
        }

        outRect.top = spacing
        outRect.bottom = spacing

        if (pos == 0) outRect.top = 0
        if (pos == (count - 1)) outRect.bottom = 0

        Timber.d("SB - $pos of $count")
    }
}


fun Fragment.hideKeyboard() {
    val activity = requireActivity()
    activity.getSystemService<InputMethodManager>()
        ?.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
}