package com.jack.nars.waver.ui.activecomposition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jack.nars.waver.R
import com.jack.nars.waver.databinding.FragmentCompositionBinding
import com.jack.nars.waver.ui.SpacingBetween
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
                addItemDecoration(SpacingBetween(resources.getDimensionPixelSize(R.dimen.list_spacing)))
            }
        }

        model.activeLoops.observe(viewLifecycleOwner) {
            loopAdapter.submitList(it)
            binding.loopList.invalidateItemDecorations()
            Timber.d("Update active loops: $it")
        }

        Timber.d("View created")

        return binding.root
    }


    private val loopAdapterListener = object : ActiveLoopAdapter.Listener {
        override fun onLoopIntensityUpdate(id: String, value: Float) {
//            Timber.v("LI - UPDATE")
            model.onPreviewLoopIntensity(id, value)

//            if (value > .85f) {
//                val i = Intent(Intent.ACTION_MAIN)
//                i.addCategory(Intent.CATEGORY_HOME)
//                startActivity(i)
//            }
        }

        override fun onLoopIntensityConfirmed(id: String, value: Float) {
//            Timber.v("LI - CONFIRM")
            model.onChangeLoopIntensity(id, value)
        }

        override fun onLoopMore(id: String, itemView: View) {
            val holder = binding.loopList.getChildViewHolder(itemView) as ActiveLoopAdapter.Holder
            holder.toggleExpansion()
        }
    }


    override fun onPause() {
        super.onPause()

        model.stopAllPreviews()
    }
}