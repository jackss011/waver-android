package com.jack.nars.waver.ui.profilelist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jack.nars.waver.databinding.ItemProfileBinding


class ProfileAdapter : ListAdapter<ProfileDisplayInfo, ProfileAdapter.Holder>(ItemCallback()) {
    var listener: Listener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemProfileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return Holder(binding)
    }


    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.run {
            bindTo(getItem(position), listener)
            binding.executePendingBindings()
        }
    }


    class Holder(val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(di: ProfileDisplayInfo, listener: Listener?) {
            binding.run {
                textProfileName.text = di.name
                textProfilePreview.text = "Placeholder for profile preview"

                root.setOnClickListener {
                    listener?.onProfileLoad(di.id)
                }

                root.setOnLongClickListener {
                    listener?.onProfileDelete(di.id) ?: false
                }
            }
        }
    }


    interface Listener {
        fun onProfileLoad(id: Long)
        fun onProfileDelete(id: Long): Boolean
    }
}


private class ItemCallback : DiffUtil.ItemCallback<ProfileDisplayInfo>() {
    override fun areItemsTheSame(
        oldItem: ProfileDisplayInfo,
        newItem: ProfileDisplayInfo
    ): Boolean {
        return newItem.id == oldItem.id
    }

    override fun areContentsTheSame(
        oldItem: ProfileDisplayInfo,
        newItem: ProfileDisplayInfo
    ): Boolean {
        return newItem == oldItem
    }
}