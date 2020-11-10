package com.jack.nars.waver.ui.looplibrary

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.LoopRepository


class LoopLibraryModel @ViewModelInject
constructor(private val loopRepository: LoopRepository) : ViewModel() {

    val staticLoops = loopRepository.staticLoops
    val activeLoopIds = loopRepository.activeCompositionData
        .map { composition -> composition.loops.map { it.id } }


    fun onLoopClicked(id: String, checked: Boolean) {
        if (checked)
            loopRepository.activateLoop(id)
        else
            loopRepository.deactivateLoop(id)
    }
}