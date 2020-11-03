package com.jack.nars.waver.data

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ControlsRepository @Inject constructor() {

    val masterVolume: MutableLiveData<Float> = MutableLiveData(1f)

    fun updateMasterVolume(volume: Float) {
        masterVolume.value = volume
    }
}