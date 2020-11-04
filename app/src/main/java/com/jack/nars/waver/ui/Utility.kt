package com.jack.nars.waver.ui

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider
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

