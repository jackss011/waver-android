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


@BindingAdapter("volume")
fun volumeAdapter(bar: SeekBar, volume: Float) {
    val newValue = min((volume * 100).roundToInt(), 100)

    if (bar.progress != newValue) {
        bar.progress = newValue
    }
}

@InverseBindingAdapter(attribute = "volume")
fun inverseVolumeAdapter(bar: SeekBar): Float {
    return min(bar.progress.toFloat() / 100f, 1f)
}


@BindingAdapter("app:volumeAttrChanged")
fun setListeners(
    bar: SeekBar,
    attrChange: InverseBindingListener
) {
    bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            attrChange.onChange()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}
