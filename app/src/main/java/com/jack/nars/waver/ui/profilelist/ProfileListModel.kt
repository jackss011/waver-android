package com.jack.nars.waver.ui.profilelist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.CompositionData
import com.jack.nars.waver.data.CompositionItem
import com.jack.nars.waver.data.database.ProfileDao
import com.jack.nars.waver.data.database.ProfileWithItems
import com.jack.nars.waver.data.repos.LoopRepository


data class ProfileDisplayInfo(
    val id: Long,
    val name: String,
)


class ProfileListModel @ViewModelInject
constructor(
    private val loopRepo: LoopRepository,
    private val profileDao: ProfileDao,
) : ViewModel() {

    val profiles = profileDao.getProfilesWithItems()

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
}


fun ProfileWithItems.toData(): CompositionData {
    return CompositionData(
        loops = this.loops.map { CompositionItem(id = it.idLoop, volume = it.intensity) }
    )
}