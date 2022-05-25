package com.jack.nars.waver.ui.profileeditor

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.jack.nars.waver.data.CompositionData
import com.jack.nars.waver.data.database.LoopInProfile
import com.jack.nars.waver.data.database.Profile
import com.jack.nars.waver.data.database.ProfileDao
import com.jack.nars.waver.data.database.ProfileWithItems
import com.jack.nars.waver.data.repos.LoopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


class Validated<T>(initial: T, private val validator: (T) -> Boolean) {
    var data: T = initial
        set(value) {
            field = value
            isValid = validator(value)
        }

    var isValid = false
        private set
}


val <T>LiveData<Validated<T>>.data: T?
    get() = value?.data


@HiltViewModel
class ProfileEditorModel @Inject
constructor(
    private val loopRepo: LoopRepository,
    private val profileDao: ProfileDao,
) : ViewModel() {

    private val loadedComposition = MutableLiveData<CompositionData?>()

    fun loadCurrentComposition() {
        val loading = loopRepo.activeCompositionData.value

        if (loading?.isPlayable != true) throw IllegalStateException("Loaded unplayable composition")
        loadedComposition.value = loading
    }


    val isSaving = MutableLiveData(false)
    val savedCorrectly = MutableLiveData<Boolean?>(null)


    val name = MutableLiveData(Validated("") { it.isNotEmpty() })
    val canCreate = name.map { it.isValid }


    fun confirmed() {
        if (canCreate.value != true || isSaving.value == true) return

        val loaded = loadedComposition.value ?: throw IllegalStateException("Composition is null")

        viewModelScope.launch {
            isSaving.value = true
            profileDao.createOrUpdateProfile(
                loaded.toDB(name.data ?: error("Invalid profile name"))
            )
            isSaving.value = false
            savedCorrectly.value = true
        }
    }


    fun onName(n: String) {
        val old = name.value
        name.value = old?.apply { data = n } ?: error("Name is null")
    }


    fun savedCorrectlyReceived() {
        if (savedCorrectly.value != null) savedCorrectly.value = null
    }
}


fun CompositionData.toDB(name: String): ProfileWithItems {
    val profile = Profile(name = name, creationTimestamp = Date().time)
    val loops = this.loops.map { LoopInProfile(idLoop = it.id, intensity = it.volume) }
    return ProfileWithItems(profile, loops)
}