package com.jack.nars.waver.data

import android.content.Context
import android.content.res.Resources
import android.media.MediaPlayer
import com.jack.nars.waver.players.BasePlayer
import java.util.*


data class Loop(
    val id: String,
    val title: String,
    val mode: Mode,
    val source: Source
) {

    enum class Mode {
        SEAMLESS,
        CROSSFADE;


        companion object {
            fun fromString(str: String): Source.Type {
                return Source.Type.valueOf(str.toUpperCase(Locale.ROOT))
            }
        }
    }


    interface Source: BasePlayer.SourceProvider {
        enum class Type {
            RES;

            companion object {
                fun fromString(str: String): Type {
                    return valueOf(str.toUpperCase(Locale.ROOT))
                }
            }

        }

        val type: Type
    }


    class Res(val id: Int): Source {
        override val type get() = Source.Type.RES

        /**
         * @throws Resources.NotFoundException
         * */
        override fun setAsDataSource(context: Context, mp: MediaPlayer) {
            val afd = context.resources.openRawResourceFd(id) ?: return
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
        }
    }
}