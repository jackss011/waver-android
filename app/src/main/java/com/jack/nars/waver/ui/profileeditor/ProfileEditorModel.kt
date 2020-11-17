package com.jack.nars.waver.ui.profileeditor

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.jack.nars.waver.data.repos.LoopRepository


class ProfileEditorModel @ViewModelInject
constructor(
    private val loopRepo: LoopRepository,
) : ViewModel() {

}