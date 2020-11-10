package com.jack.nars.waver.ui.activecomposition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.jack.nars.waver.databinding.ItemActiveLoopBinding
import com.jack.nars.waver.ui.setupAsIntensity
import timber.log.Timber


class ActiveLoopAdapter : ListAdapter<LoopDisplayInfo, ActiveLoopAdapter.Holder>(ItemCallback()) {
    var listener: Listener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemActiveLoopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        Timber.d("Created view holder")

        return Holder(binding)
    }


    override fun onBindViewHolder(holder: Holder, position: Int) {
        Timber.d("Binding view holder: ${getItem(position)}")

        holder.run {
            bindTo(getItem(position), listener)
            binding.executePendingBindings()
        }
    }


    class Holder(val binding: ItemActiveLoopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(di: LoopDisplayInfo, listener: Listener?) {
            binding.titleTxt.text = di.title

            binding.intensitySlider.run {
                value = di.intensity

                setupAsIntensity()

                clearOnChangeListeners()
                clearOnSliderTouchListeners()

                addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: Slider) {}

                    override fun onStopTrackingTouch(slider: Slider) {
                        listener?.onLoopIntensityConfirmed(di.id, slider.value)
                    }
                })

                addOnChangeListener { _: Slider, value: Float, fromUser: Boolean ->
//                    Timber.v("LI - from user: ${fromUser}")
                    if (fromUser)
                        listener?.onLoopIntensityUpdate(di.id, value)
                }
            }
        }
    }


    interface Listener {
        fun onLoopIntensityUpdate(id: String, value: Float)
        fun onLoopIntensityConfirmed(id: String, value: Float)
    }
}


private class ItemCallback : DiffUtil.ItemCallback<LoopDisplayInfo>() {
    override fun areItemsTheSame(oldItem: LoopDisplayInfo, newItem: LoopDisplayInfo): Boolean {
        return newItem.id == oldItem.id
    }

    override fun areContentsTheSame(oldItem: LoopDisplayInfo, newItem: LoopDisplayInfo): Boolean {
        return newItem == oldItem
    }
}
