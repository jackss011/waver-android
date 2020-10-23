package com.jack.nars.waver.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jack.nars.waver.databinding.ItemLoopBinding
import timber.log.Timber
import java.lang.StrictMath.round


class LoopAdapter(private val listener: Listener? = null) :
    ListAdapter<DisplayLoop, LoopAdapter.Holder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        Timber.i("FIRE - view created")
        val inflater = LayoutInflater.from(parent.context)
        return Holder(ItemLoopBinding.inflate(inflater, parent, false))
    }


    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindTo(getItem(position) ?: return, listener)
    }


    class Holder(private val binding: ItemLoopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(loop: DisplayLoop, listener: Listener?) {
            Timber.i("FIRE - bindTo ${loop.loop.id}")
            binding.loop = loop
            binding.itemSeek.progress = round(loop.intensity * 100)
            binding.executePendingBindings()

            if (listener == null) return

            binding.itemSwitch.setOnCheckedChangeListener { _, isChecked ->
                val displayLoop = binding.loop ?: return@setOnCheckedChangeListener

                Timber.i("FIRE - Checkbox listener. Checked: $isChecked")
                listener.onLoopUpdated(displayLoop.loop.id, isChecked, displayLoop.intensity)
            }

            binding.itemSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (!fromUser || seekBar == null) return

                    val displayLoop = binding.loop ?: return
                    val intensity = progress.toFloat() / seekBar.max
                    listener.onLoopUpdated(displayLoop.loop.id, displayLoop.enabled, intensity)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }


    class Diff : DiffUtil.ItemCallback<DisplayLoop>() {
        override fun areItemsTheSame(oldItem: DisplayLoop, newItem: DisplayLoop): Boolean {
            Timber.i("Asking for same ITEM: $oldItem -> $newItem\n return ${oldItem.loop.id == newItem.loop.id}")
            return newItem.loop.id == oldItem.loop.id
        }

        override fun areContentsTheSame(oldItem: DisplayLoop, newItem: DisplayLoop): Boolean {
            Timber.i("Asking for same CONTENT: $oldItem -> $newItem\n return ${oldItem == newItem}")
            return newItem == oldItem
        }
    }


    class Listener(val listener: (id: String, enabled: Boolean, intensity: Float) -> Unit) {
        fun onLoopUpdated(id: String, enabled: Boolean, intensity: Float) =
            listener(id, enabled, intensity)
    }
}