package com.jack.nars.waver.bottombar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.slider.Slider
import com.jack.nars.waver.MainModel
import com.jack.nars.waver.R
import com.jack.nars.waver.databinding.FragmentBottomBarBinding
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Math.round
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt


@AndroidEntryPoint
class BottomBarFragment : Fragment() {
    private val viewModel: BottomBarModel by viewModels()
    private val mainModel: MainModel by activityViewModels()
    private lateinit var binding: FragmentBottomBarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentBottomBarBinding>(
            inflater,
            R.layout.fragment_bottom_bar,
            container,
            false
        ).apply {
            lifecycleOwner = this@BottomBarFragment
            modelView = viewModel
            modelMain = mainModel

            playPause.setOnClickListener {
                mainModel.onPlayPause()
            }
        }

        return binding.root
    }
}


// Get value from VM
@BindingAdapter("bindUnitaryValue")
fun volumeAdapter(bar: Slider, value: Float) {
    if (bar.value != value) {
        bar.value = value
    }
}


// Set value to VM
@InverseBindingAdapter(attribute = "bindUnitaryValue")
fun inverseVolumeAdapter(bar: Slider): Float {
    return bar.value.coerceIn(0f, 1f)
}


@BindingAdapter("app:bindUnitaryValueAttrChanged")
fun setListeners(bar: Slider, attrChange: InverseBindingListener) {
    bar.addOnChangeListener { _, _, _ -> attrChange.onChange() }
}