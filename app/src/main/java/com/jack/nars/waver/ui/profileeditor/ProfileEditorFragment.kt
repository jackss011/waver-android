package com.jack.nars.waver.ui.profileeditor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.jack.nars.waver.R
import com.jack.nars.waver.databinding.FragmentProfileEditorBinding
import com.jack.nars.waver.databinding.FragmentProfileEditorBindingImpl


class ProfileEditorFragment : Fragment() {
    val model: ProfileEditorModel by viewModels()
    private lateinit var binding: FragmentProfileEditorBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileEditorBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        return binding.root
    }
}