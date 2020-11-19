package com.jack.nars.waver.ui.profilelist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.jack.nars.waver.R
import com.jack.nars.waver.databinding.FragmentProfileListBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class ProfileListFragment : Fragment() {
    private val model: ProfileListModel by viewModels()
    private lateinit var binding: FragmentProfileListBinding
    private lateinit var adapter: ProfileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = ProfileAdapter()

        binding = FragmentProfileListBinding.inflate(inflater, container, false).apply {
            listProfiles.layoutManager = LinearLayoutManager(requireActivity())
            listProfiles.adapter = adapter
        }

        model.profileDisplay.observe(viewLifecycleOwner) {
            Timber.d("Profile updated: $it")

            adapter.submitList(it)
        }

        return binding.root
    }
}