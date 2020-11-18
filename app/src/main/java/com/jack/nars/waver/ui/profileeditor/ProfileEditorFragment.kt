package com.jack.nars.waver.ui.profileeditor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.jack.nars.waver.R
import com.jack.nars.waver.databinding.FragmentProfileEditorBinding
import com.jack.nars.waver.databinding.FragmentProfileEditorBindingImpl


class ProfileEditorFragment : Fragment() {
    private val model: ProfileEditorModel by viewModels()
    private lateinit var binding: FragmentProfileEditorBinding
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfileEditorBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@ProfileEditorFragment

            btnCreate.setOnClickListener {
                val name = binding.textField.editText?.text.toString()
                model.confirmed(name)
            }

            btnBack.setOnClickListener {
                navController.popBackStack()
            }
        }

        model.also { m ->
            m.isSaving.observe(viewLifecycleOwner) {
                showProgress(it)
            }

            m.savedCorrectly.observe(viewLifecycleOwner) {
                when (it) {
                    true -> {
                        showSaveSuccess()
                        navController.popBackStack()
                    }
                    false -> showSaveError()
                }

                m.savedCorrectlyReceived()
            }
        }

        return binding.root
    }


    override fun onStart() {
        super.onStart()

        model.loadCurrentComposition()
    }


    private fun showProgress(show: Boolean) {

    }


    private fun showSaveError() {

    }


    private fun showSaveSuccess() {

    }
}