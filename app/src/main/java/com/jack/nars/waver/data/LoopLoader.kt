package com.jack.nars.waver.data

import android.content.Context
import android.content.res.XmlResourceParser
import com.jack.nars.waver.BuildConfig
import com.jack.nars.waver.R
import com.jack.nars.waver.sound.Loop
import org.xmlpull.v1.XmlPullParser


object LoopLoader {
//    fun attributeMap(parser: XmlPullParser): Map<String, String> {
//        val entries = arrayOfNulls<Pair<String, String>>(parser.attributeCount)
//
//        for(i in 0 until parser.attributeCount)
//            entries[i] = parser.getAttributeName(i) to parser.getAttributeValue(i)
//
//        return (entries as Array<Pair<String, String>>).toMap()
//    }


    fun getAllLoops(context: Context): Iterable<Loop> {


        fun parseSource(parser: XmlResourceParser): Loop.Source {
            var id: Int? = null

            for (i in 0 until parser.attributeCount)
                when (parser.getAttributeName(i)) {
                    "id" -> id = parser.getAttributeResourceValue(i, 0)
                }

            if (BuildConfig.DEBUG && parser.next() != XmlPullParser.END_TAG) {
                error("Assertion failed")
            }

            return Loop.Res(id!!)
        }


        fun parseLoop(parser: XmlResourceParser): Loop {
            var id: String? = null
            var title: String? = null
            var source: Loop.Source? = null

            for(i in 0 until parser.attributeCount)
                when(parser.getAttributeName(i)) {
                    "id" -> id = parser.getAttributeValue(i)
                    "title" -> title = parser.getAttributeValue(i)
                }

            while(parser.next() != XmlPullParser.END_TAG) {
                if(parser.eventType == XmlPullParser.START_TAG) {
                    when(parser.name) {
                        "source" -> {
                            source = parseSource(parser)
                        }
                    }
                }
            }

            return Loop(id!!, title!!, Loop.Mode.CROSSFADE, source!!)
        }


        fun parseLoops(parser: XmlResourceParser): Iterable<Loop> {
            val r = mutableListOf<Loop>()

            while(parser.next() != XmlPullParser.END_DOCUMENT) {
                if(parser.eventType == XmlPullParser.START_TAG) {
                    when(parser.name) {
                        "loop" ->
                            r.add(parseLoop(parser))
                    }
                }
            }

            return r
        }


        return parseLoops(context.resources.getXml(R.xml.loops))
    }
}