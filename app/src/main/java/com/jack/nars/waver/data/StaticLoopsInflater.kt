package com.jack.nars.waver.data

import android.content.Context
import android.content.res.XmlResourceParser
import com.jack.nars.waver.BuildConfig
import com.jack.nars.waver.R
import org.xmlpull.v1.XmlPullParser


object StaticLoopsInflater {
    private fun parseSource(parser: XmlResourceParser): Loop.Source {
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


    private fun parseLoop(parser: XmlResourceParser): Loop {
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


    private fun parseLoops(parser: XmlResourceParser): Iterable<Loop> {
        return mutableListOf<Loop>().apply {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "loop" ->
                            add(parseLoop(parser))
                    }
                }
            }
        }
    }


    fun inflate(context: Context): Iterable<Loop> = parseLoops(context.resources.getXml(R.xml.loops))
}