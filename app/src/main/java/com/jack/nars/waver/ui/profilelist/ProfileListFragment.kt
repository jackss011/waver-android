package com.jack.nars.waver.ui.profilelist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
    ): View {
        adapter = ProfileAdapter().apply {
            listener = object : ProfileAdapter.Listener {
                override fun onProfileLoad(id: Long) {
                    model.activateProfile(id)

                    Snackbar.make(
                        container!!,
                        getString(R.string.snack_profile_loaded),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                override fun onProfileDelete(id: Long): Boolean {
                    showDeleteDialog(deleteId = id)
                    return true
                }
            }
        }

        binding = FragmentProfileListBinding.inflate(inflater, container, false).apply {
            listProfiles.layoutManager = LinearLayoutManager(requireActivity())
            listProfiles.adapter = adapter
        }

        model.profileDisplay.observe(viewLifecycleOwner) {
            Timber.d("Profile updated: $it")

            adapter.submitList(it)
            showNoProfiles(it.isEmpty())
        }

        return binding.root
    }


    private fun showDeleteDialog(deleteId: Long) {
        val item = model.getProfileById(deleteId) ?: error("Deleting wrong profile")

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(R.string.profile_delete_title, item.name))
            .setMessage(getString(R.string.profile_delete_msg))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                model.deleteProfile(deleteId)
                Snackbar.make(
                    requireView(),
                    getString(R.string.snack_profile_deleted),
                    Snackbar.LENGTH_SHORT
                ).show()
            }.setNegativeButton(getString(R.string.btn_back)) { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }


    private fun showNoProfiles(show: Boolean) {
        binding.notProfileHint.visibility = if (show) View.VISIBLE else View.GONE
    }
}