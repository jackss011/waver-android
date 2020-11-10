package com.jack.nars.waver.ui.activecomposition

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jack.nars.waver.R
import com.jack.nars.waver.databinding.FragmentCompositionBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class CompositionFragment : Fragment() {
    private val model: CompositionModel by viewModels()
    private lateinit var binding: FragmentCompositionBinding

    private lateinit var loopAdapter: ActiveLoopAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        loopAdapter = ActiveLoopAdapter().apply {
            listener = loopAdapterListener
        }

        binding = FragmentCompositionBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = requireActivity()

            loopList.apply {
                adapter = loopAdapter
                layoutManager = LinearLayoutManager(requireActivity())
            }
        }

        model.activeLoops.observe(viewLifecycleOwner) {
            loopAdapter.submitList(it)
            Timber.d("Update active loops: $it")
        }

        Timber.d("View created")

        return binding.root
    }


    private val loopAdapterListener = object : ActiveLoopAdapter.Listener {
        override fun onLoopIntensityUpdate(id: String, value: Float) {
            model.onPreviewLoopIntensity(id, value)
        }

        override fun onLoopIntensityConfirmed(id: String, value: Float) {
            model.onChangeLoopIntensity(id, value)
        }
    }
}