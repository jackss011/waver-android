package com.jack.nars.waver.ui.bottombar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.jack.nars.waver.R
import com.jack.nars.waver.databinding.FragmentBottomBarBinding
import com.jack.nars.waver.ui.setupAsIntensity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BottomBarFragment : Fragment() {
    private val model: BottomBarModel by viewModels()
    private lateinit var binding: FragmentBottomBarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate<FragmentBottomBarBinding>(
            inflater,
            R.layout.fragment_bottom_bar,
            container,
            false
        ).apply {
            lifecycleOwner = this@BottomBarFragment
            boundModel = model
        }

        binding.playPause.setOnClickListener {
            model.onPlayPause()
        }

        binding.volumeBar.apply {
            setupAsIntensity()
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    model.onMasterVolumeConfirmed(slider.value)
                }
            })
        }

        return binding.root
    }
}


@BindingAdapter("playIcon")
fun bindPlayIcon(btn: MaterialButton, isPlaying: Boolean?) {
    isPlaying?.let {
        btn.icon = ResourcesCompat.getDrawable(
            btn.context.resources,
            if (!isPlaying) R.drawable.ic_play_24 else R.drawable.ic_pause_24,
            null
        )
    }
}