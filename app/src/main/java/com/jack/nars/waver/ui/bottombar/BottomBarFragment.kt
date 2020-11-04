package com.jack.nars.waver.ui.bottombar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.jack.nars.waver.ui.setupAsIntensity
import dagger.hilt.android.AndroidEntryPoint


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

            volumeBar.setupAsIntensity()
        }

        return binding.root
    }
}