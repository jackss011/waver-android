package com.jack.nars.waver.list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.Loop
import com.jack.nars.waver.data.LoopRepository
import timber.log.Timber


data class LoopInfo(
    val id: String,
    val title: String,
)

data class LoopControls(
    val enabled: Boolean = false,
    val intensity: Float = .5f,
)


class LoopListModel @ViewModelInject
constructor(private val loopRepository: LoopRepository) : ViewModel() {

    private val staticLoops = loopRepository.staticLoops.toList()


    private val _displayedLoops: MutableLiveData<List<LoopInfo>> =
        MutableLiveData(staticLoops.map { LoopInfo(it.id, it.title) }.toList())

    val displayLoops: LiveData<List<LoopInfo>> get() = _displayedLoops


    private val _loopControls: MutableLiveData<List<LoopControls>> =
        MutableLiveData(displayLoops.value?.map { LoopControls() }?.toList())

    private val loopControls: LiveData<List<LoopControls>> get() = _loopControls


    init {
        Timber.i("${loopControls.value}")
    }


    private fun positionOf(id: String) = displayLoops.value?.indexOfFirst { id == it.id } ?: -1


    fun getControls(position: Int): LoopControls? {
        return loopControls.value?.get(position)
    }


    fun getControls(id: String): LoopControls? {
        return getControls(positionOf(id))
    }


    fun onLoopIntensity(id: String, intensity: Float) {
        updateControl(id) { it.copy(intensity = intensity) }
    }


    fun onLoopEnabled(id: String, enabled: Boolean) {
        updateControl(id) { it.copy(enabled = enabled) }
    }


    private fun updateControl(id: String, updater: (LoopControls) -> LoopControls) {
        val pos = positionOf(id)
        _loopControls.value = loopControls.value?.mapIndexed { index, it ->
            if (index == pos) updater(it) else it.copy()
        }
    }
}