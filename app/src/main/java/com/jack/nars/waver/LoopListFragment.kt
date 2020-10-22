package com.jack.nars.waver

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jack.nars.waver.data.LoopRepository
import com.jack.nars.waver.databinding.FragmentLoopListBinding
import com.jack.nars.waver.databinding.ItemLoopBinding
import com.jack.nars.waver.sound.Loop
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class LoopListFragment : Fragment() {
    private lateinit var binding: FragmentLoopListBinding
    @Inject lateinit var loopRepository: LoopRepository


    class LoopViewHolder (private val binding: ItemLoopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindTo(loop: Loop) {
            binding.itemTitle.text = loop.title
        }
    }


    class LoopAdapter(val context: Context) : RecyclerView.Adapter<LoopViewHolder>() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        var dataset: List<Loop>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoopViewHolder {
            return LoopViewHolder(ItemLoopBinding.inflate(inflater, parent, false))
        }

        override fun onBindViewHolder(holder: LoopViewHolder, position: Int) {
            holder.bindTo(dataset?.get(position) ?: return)
        }

        override fun getItemCount() = dataset?.size ?: 0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("Created")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val context = requireContext()
        val adapter = LoopAdapter(context)

        binding = FragmentLoopListBinding
            .inflate(inflater, container, false).apply {

            loopList.apply {
                this.adapter = adapter
                layoutManager = LinearLayoutManager(context)
            }
        }

        adapter.dataset = loopRepository.staticLoops.toList()

        return binding.root
    }
}