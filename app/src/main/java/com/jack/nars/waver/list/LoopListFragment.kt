package com.jack.nars.waver.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jack.nars.waver.databinding.FragmentLoopListBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class LoopListFragment : Fragment() {
    private lateinit var binding: FragmentLoopListBinding
    private val viewModel: LoopListModel by viewModels()

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

        adapter.dataset = viewModel.staticLoops

        return binding.root
    }
}