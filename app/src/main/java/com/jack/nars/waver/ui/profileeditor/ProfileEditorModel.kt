package com.jack.nars.waver.ui.profileeditor

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jack.nars.waver.data.CompositionData
import com.jack.nars.waver.data.database.LoopInProfile
import com.jack.nars.waver.data.database.Profile
import com.jack.nars.waver.data.database.ProfileDao
import com.jack.nars.waver.data.database.ProfileWithItems
import com.jack.nars.waver.data.repos.LoopRepository
import kotlinx.coroutines.launch
import java.util.*


class ProfileEditorModel @ViewModelInject
constructor(
    private val loopRepo: LoopRepository,
    private val profileDao: ProfileDao,
) : ViewModel() {

    private val loadedComposition = MutableLiveData<CompositionData?>()

    val isSaving = MutableLiveData(false)
    val savedCorrectly = MutableLiveData<Boolean?>(null)

    fun loadCurrentComposition() {
        loadedComposition.value = loopRepo.activeCompositionData.value
    }


    fun confirmed(name: String) {
        val loaded = loadedComposition.value ?: throw IllegalStateException("Composition is null")

        viewModelScope.launch {
            isSaving.value = true
            profileDao.createOrUpdateProfile(loaded.toDB(name))
            isSaving.value = false
            savedCorrectly.value = true
        }
    }


    fun savedCorrectlyReceived() {
        savedCorrectly.value = null
    }
}


fun CompositionData.toDB(name: String): ProfileWithItems {
    val profile = Profile(name = name, creationTimestamp = Date().time)
    val loops = this.loops.map { LoopInProfile(idLoop = it.id, intensity = it.volume) }
    return ProfileWithItems(profile, loops)
}