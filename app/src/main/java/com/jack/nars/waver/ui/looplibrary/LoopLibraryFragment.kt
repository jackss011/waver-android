package com.jack.nars.waver.ui.looplibrary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.jack.nars.waver.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class LoopLibraryFragment : Fragment() {
    private val model: LoopLibraryModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loop_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroup = getView()?.findViewById<ChipGroup>(R.id.chipgroup) ?: return
        val inflater = LayoutInflater.from(requireActivity())

        fun createBlankChip() = inflater.inflate(
            R.layout.item_loop_chip, chipGroup, false
        ) as Chip

        model.staticLoops.forEach { loop ->
            val chip = createBlankChip().apply {
                text = loop.title
                tag = loop.id
                setOnClickListener {
                    Timber.d("Clicked: ${loop.id}, set to ${this.isChecked}}")
                    model.onLoopClicked(loop.id, this.isChecked)
                }
            }

            chipGroup.addView(chip)
        }

        model.activeLoopIds.observe(requireActivity()) { activeIds ->
            model.staticLoops.forEach { loop ->
                chipGroup.findViewWithTag<Chip>(loop.id)?.also { chip ->
                    val active = loop.id in activeIds
                    chip.isChecked = active
                    Timber.d("Setting chip ${loop.id} to $active")
                }
            }
        }
    }
}