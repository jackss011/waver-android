package com.jack.nars.waver.list

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.databinding.BaseObservable
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.InverseBindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jack.nars.waver.R
import com.jack.nars.waver.data.Loop
import com.jack.nars.waver.databinding.ItemLoopBinding
import kotlinx.android.synthetic.main.item_loop.view.*
import timber.log.Timber
import kotlin.math.round


class LoopAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModel: LoopListModel,
) : ListAdapter<LoopInfo, LoopAdapter.Holder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        Timber.i("FIRE - view created")

        val inflater = LayoutInflater.from(parent.context)

        val binding: ItemLoopBinding =
            DataBindingUtil.inflate(inflater, R.layout.item_loop, parent, false)
        binding.lifecycleOwner = lifecycleOwner

        return Holder(binding)
    }


    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindTo(getItem(position), viewModel.getControls(position), viewModel)
    }



    class Holder(private val binding: ItemLoopBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindTo(info: LoopInfo, controls: LoopControls?, viewModel: LoopListModel) {
            binding.apply {
                this.info = info
                executePendingBindings()
            }

            if (controls == null) throw IllegalStateException("Missing LoopControls")

            binding.apply {
                itemSwitch.apply {
                    isChecked = controls.enabled

                    setOnCheckedChangeListener { _, isChecked ->
                        viewModel.onLoopEnabled(info.id, isChecked)
                    }
                }

                itemSeek.apply {
                    progress = round(controls.intensity * itemSeek.max).toInt()

                    setUpdateListener {
                        viewModel.onLoopIntensity(info.id, it)
                    }
                }
            }
        }
    }


    class Diff : DiffUtil.ItemCallback<LoopInfo>() {
        override fun areItemsTheSame(oldItem: LoopInfo, newItem: LoopInfo): Boolean {
            Timber.i("Asking for same ITEM: $oldItem -> $newItem\n return ${oldItem.id == newItem.id}")
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(oldItem: LoopInfo, newItem: LoopInfo): Boolean {
            Timber.i("Asking for same CONTENT: $oldItem -> $newItem\n return ${oldItem == newItem}")
            return newItem == oldItem
        }
    }
}


fun SeekBar.setUpdateListener(callback: (intensity: Float) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            callback(progress.toFloat() / max)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}


//@BindingAdapter("title")
//fun title(tw: TextView, data: LoopAdapter.ItemData?) {
//    if (data != null) {
//        tw.text = data.viewModel.displayLoops.value?.get(data.position)?.title ?: return
//    }
//}
//
//
//@BindingAdapter("prg")
//fun setPrg(view: SeekBar, data: LoopAdapter.ItemData?) {
//    if (data != null) {
//
//    }
//}
//
//@InverseBindingAdapter(attribute = "prg")
//fun getPrg(view: SeekBar): LoopAdapter.ItemData? {
////    return view.getTime()
//}

//        fun bindTo(viewModel: LoopListModel, position: Int) {
//
//            val ld = Transformations.map(viewModel.displayLoops) {
//                Timber.w("GUCK")
//                ItemData(viewModel, position)
//            }
//
//            val w = ItemDataWrapper(ld)
//            binding.watch = w
//            binding.executePendingBindings()
////            binding.invalidateAll()
//
//
////            ld.observe(binding.lifecycleOwner)
//
//            Timber.w("bind to: ${ld} -> ${ld.value}")
//        }

//        fun bindTo(loop: DisplayLoop, listener: Listener?) {
//            Timber.i("FIRE - bindTo ${loop.id}")
//            binding.executePendingBindings()
//
//            if (listener == null) return
//
//            binding.itemSwitch.setOnCheckedChangeListener { _, isChecked ->
//                val displayLoop = binding.loop ?: return@setOnCheckedChangeListener
//
//                Timber.i("FIRE - Checkbox listener. Checked: $isChecked")
//                listener.onLoopUpdated(displayLoop.loop.id, isChecked, displayLoop.intensity)
//            }
//
//            binding.itemSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                override fun onProgressChanged(
//                    seekBar: SeekBar?,
//                    progress: Int,
//                    fromUser: Boolean
//                ) {
//                    if (!fromUser || seekBar == null) return
//
//                    val displayLoop = binding.loop ?: return
//                    val intensity = progress.toFloat() / seekBar.max
//                    listener.onLoopUpdated(displayLoop.loop.id, displayLoop.enabled, intensity)
//                }
//
//                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//            })
//        }
//
//
//class Listener(val listener: (id: String, enabled: Boolean, intensity: Float) -> Unit) {
//    fun onLoopUpdated(id: String, enabled: Boolean, intensity: Float) =
//        listener(id, enabled, intensity)
//}