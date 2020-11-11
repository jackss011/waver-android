package com.jack.nars.waver.ui.activecomposition

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.jack.nars.waver.R
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


    override fun onViewDetachedFromWindow(holder: Holder) {
        super.onViewDetachedFromWindow(holder)

        holder.unbind()
    }


    class Holder(val binding: ItemActiveLoopBinding) : RecyclerView.ViewHolder(binding.root) {

        private val popup: PopupMenu by lazy {
            val wrapper = ContextThemeWrapper(binding.root.context, R.style.Widget_Waver_PopupMenu)
            val popup = PopupMenu(wrapper, itemView.findViewById(R.id.expandBtn))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                popup.setForceShowIcon(true)
            }
            popup.menuInflater.inflate(R.menu.active_loop_options, popup.menu)
            popup
        }


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

            binding.expandBtn.setOnClickListener {
                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_loop_remove -> listener?.onRemoveLoop(di.id)
                    }
                    true
                }

                popup.show()
            }
        }


        fun unbind() {
            popup.dismiss()
        }
    }


    interface Listener {
        fun onLoopIntensityUpdate(id: String, value: Float) {}
        fun onLoopIntensityConfirmed(id: String, value: Float) {}
        fun onLoopMore(id: String, itemView: View) {}
        fun onRemoveLoop(id: String) {}
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


//        fun toggleExpansion() {
//            setExpansion(!isExpanded)
//        }
//
//        val isExpanded get() = binding.listBtns.visibility == View.VISIBLE
//
//        fun setExpansion(expand: Boolean) {
//            binding.expandBtn.rotation = if(expand) 180f else 0f
//            binding.listBtns.visibility = if(expand) View.VISIBLE else View.GONE
//        }
