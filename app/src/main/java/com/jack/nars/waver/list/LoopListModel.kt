package com.jack.nars.waver.list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.jack.nars.waver.data.CompositionData
import com.jack.nars.waver.data.CompositionItem
import com.jack.nars.waver.data.Loop
import com.jack.nars.waver.data.LoopRepository
import timber.log.Timber
import java.lang.IllegalStateException


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


    private val _loopControls: MutableLiveData<Map<String, LoopControls>> =
        MutableLiveData((displayLoops.value?.map { it.id to LoopControls() })?.toMap())

    private val loopControls: LiveData<List<LoopControls>> get() = _loopControls.map { it.values.toList() }


    init {
        Timber.i("${loopControls.value}")
    }


    private fun positionOf(id: String) = displayLoops.value?.indexOfFirst { id == it.id } ?: -1


    fun getControls(id: String): LoopControls? {
        return _loopControls.value?.get(id)
    }


    fun onLoopIntensity(id: String, intensity: Float) {
        updateControl(id) { it.copy(intensity = intensity) }
    }


    fun onLoopEnabled(id: String, enabled: Boolean) {
        updateControl(id) { it.copy(enabled = enabled) }
    }


    private fun updateControl(id: String, updater: (LoopControls) -> LoopControls) {
        _loopControls.value = _loopControls.value?.mapValues { (key, value) ->
            if (key == id) updater(value) else value.copy()
        }

        updateComposition()
    }


    private fun calculateComposition(): CompositionData {
        val loops = (_loopControls.value ?: throw IllegalStateException())
            .filter { (_, c) -> c.enabled }
            .map { (id, c) -> CompositionItem(id, c.intensity) }

        return CompositionData(loops)
    }


    private fun updateComposition() {
        val c = calculateComposition()

        loopRepository.updateActiveComposition(c)
    }
}