package com.jack.nars.waver.list

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jack.nars.waver.data.LoopRepository
import timber.log.Timber


data class DisplayLoop(
    val id: String,
    val title: String,
    val enabled: Boolean,
    val intensity: Float
)


class LoopListModel @ViewModelInject
constructor(private val loopRepository: LoopRepository) : ViewModel() {

    private val staticLoops = loopRepository.staticLoops.toList()

    private val _displayedLoops: MutableLiveData<List<DisplayLoop>> =
        MutableLiveData(staticLoops.map {
            DisplayLoop(it.id, it.title, false, 1f)
        }.toList())
    val displayLoops: LiveData<List<DisplayLoop>> get() = _displayedLoops


    private fun setLoopEnabled(id: String, newEnabled: Boolean) {
        val new = _displayedLoops.value?.map {
            if (id == it.id)
                it.copy(enabled = newEnabled)
            else
                it.copy()
        }

        _displayedLoops.value = new

        Timber.i("FIRE - Loop enabled $newEnabled")
    }


    private fun changeLoopIntensity(id: String, newIntensity: Float) {
        val new = _displayedLoops.value?.map {
            if (id == it.id)
                it.copy(intensity = newIntensity)
            else
                it.copy()
        }

        _displayedLoops.value = new
    }


    private fun getCompositionData() {}
}