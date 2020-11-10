package com.jack.nars.waver.ui.activecomposition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jack.nars.waver.databinding.ItemActiveLoopBinding
import timber.log.Timber


class ActiveLoopAdapter : ListAdapter<LoopDisplayInfo, ActiveLoopAdapter.Holder>(ItemCallback()) {
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
        holder.run {
            bindTo(getItem(position))
            binding.executePendingBindings()
        }
    }


    class Holder(val binding: ItemActiveLoopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(di: LoopDisplayInfo) {
            binding.titleTxt.text = di.title
        }
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
