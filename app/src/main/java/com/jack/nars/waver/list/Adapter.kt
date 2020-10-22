package com.jack.nars.waver.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jack.nars.waver.databinding.ItemLoopBinding
import com.jack.nars.waver.sound.Loop


class LoopAdapter(val context: Context) : RecyclerView.Adapter<LoopAdapter.Holder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    var dataset: List<Loop>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemLoopBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindTo(dataset?.get(position) ?: return)
    }

    override fun getItemCount() = dataset?.size ?: 0


    class Holder (private val binding: ItemLoopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(loop: Loop) {
            binding.itemTitle.text = loop.title
        }
    }
}