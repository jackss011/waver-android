package com.jack.nars.waver.ui.profileeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jack.nars.waver.R
import com.jack.nars.waver.databinding.FragmentProfileEditorBinding
import com.jack.nars.waver.ui.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfileEditorFragment : Fragment() {
    private val model: ProfileEditorModel by viewModels()
    private lateinit var binding: FragmentProfileEditorBinding
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileEditorBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@ProfileEditorFragment

            btnCreate.setOnClickListener { model.confirmed() }
            btnBack.setOnClickListener { navController.popBackStack() }

            editProfileName.run {
                doOnTextChanged { txt, _, _, _ -> model.onName(txt.toString()) }
                setText(model.name.data)
            }
        }

        model.run {
            isSaving.observe(viewLifecycleOwner) { showProgress(it) }

            savedCorrectly.observe(viewLifecycleOwner) {
                when (it) {
                    true -> {
                        showSaveSuccess()
                        navController.popBackStack()
                    }
                    else -> showSaveError()
                }

                savedCorrectlyReceived()
            }

            canCreate.observe(viewLifecycleOwner) { binding.btnCreate.isEnabled = (it == true) }
        }

        return binding.root
    }


    override fun onStart() {
        super.onStart()

        model.loadCurrentComposition()
    }

    override fun onPause() {
        super.onPause()

        hideKeyboard()
    }

    private fun showProgress(show: Boolean) {
        binding.progressSaving.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }


    private fun showSaveSuccess() {
        view?.let {
            Snackbar.make(
                it,
                getString(R.string.snack_profile_created),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }


    private fun showSaveError() {
        view?.let {
            Snackbar.make(
                it,
                getString(R.string.snack_profile_create_error),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}