package com.jack.nars.waver.data.storage

import android.content.Context
import com.jack.nars.waver.data.CompositionData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject


class LastStateStorage @Inject constructor(@ApplicationContext val context: Context) {
    private val sharedPref = context.getSharedPreferences(PREF_LAST_STORAGE, Context.MODE_PRIVATE)

    private companion object {
        const val PREF_LAST_STORAGE = "com.jack.nars.waver.LAST_STATE_STORAGE_PREF"
        const val KEY_ACTIVE_COMPOSITION = "KEY_ACTIVE_COMPOSITION"
        const val KEY_MASTER_VOLUME = "KEY_MASTER_VOLUME"
    }


//    val activeComposition: CompositionData?
//        get() {
//            val s = sharedPref.getString(KEY_ACTIVE_COMPOSITION, null) ?: return null
//            return Json.decodeFromString<CompositionData>(s)
//        }

    fun getActiveComposition(): CompositionData? {
        val s = sharedPref.getString(KEY_ACTIVE_COMPOSITION, null) ?: return null
        return Json.decodeFromString<CompositionData>(s)
    }


    fun saveActiveComposition(c: CompositionData) {
        sharedPref.edit()
            .putString(KEY_ACTIVE_COMPOSITION, Json.encodeToString(c))
            .apply()
    }


    fun saveMasterVolume(v: Float) {
        sharedPref.edit()
            .putFloat(KEY_MASTER_VOLUME, v)
            .apply()
    }


    fun getMasterVolume(): Float {
        return sharedPref.getFloat(KEY_MASTER_VOLUME, 1f)
    }
}