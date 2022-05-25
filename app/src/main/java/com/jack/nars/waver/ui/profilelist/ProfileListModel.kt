package com.jack.nars.waver.ui.profilelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.jack.nars.waver.data.CompositionData
import com.jack.nars.waver.data.CompositionItem
import com.jack.nars.waver.data.database.ProfileDao
import com.jack.nars.waver.data.database.ProfileWithItems
import com.jack.nars.waver.data.repos.LoopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ProfileDisplayInfo(
    val id: Long,
    val name: String,
)

@HiltViewModel
class ProfileListModel @Inject
constructor(
    private val loopRepo: LoopRepository,
    private val profileDao: ProfileDao,
) : ViewModel() {

    private val profiles = profileDao.getProfilesWithItems()

    val profileDisplay = profiles.map { list ->
        list.map {
            ProfileDisplayInfo(it.profile.idProfile, it.profile.name)
        }
    }


    fun activateProfile(id: Long) {
        val list = profiles.value!!
        val selected = list.find { it.profile.idProfile == id }
        loopRepo.updateActiveComposition(selected!!.toData())
    }


    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            profileDao.deleteProfile(id)
        }
    }


    fun getProfileById(id: Long): ProfileDisplayInfo? {
        return profileDisplay.value?.find { id == it.id }
    }
}


fun ProfileWithItems.toData(): CompositionData {
    return CompositionData(
        loops = this.loops.map { CompositionItem(id = it.idLoop, volume = it.intensity) }
    )
}