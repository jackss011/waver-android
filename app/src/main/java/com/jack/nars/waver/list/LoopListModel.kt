package com.jack.nars.waver.list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.jack.nars.waver.data.LoopRepository


class LoopListModel @ViewModelInject
    constructor(private val loopRepository: LoopRepository) : ViewModel() {

    val staticLoops = loopRepository.staticLoops.toList()
}