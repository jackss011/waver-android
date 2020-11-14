package com.jack.nars.waver.data

import android.content.ContentResolver
import android.content.Context
import android.content.res.XmlResourceParser
import com.jack.nars.waver.BuildConfig
import com.jack.nars.waver.R
import org.xmlpull.v1.XmlPullParser
import java.io.File


object StaticLoopsInflater {
    private fun parseSource(context: Context, parser: XmlResourceParser): String {
        var id: Int? = null

        for (i in 0 until parser.attributeCount)
            when (parser.getAttributeName(i)) {
                "id" -> id = parser.getAttributeResourceValue(i, 0)
            }

        if (BuildConfig.DEBUG && parser.next() != XmlPullParser.END_TAG) {
            error("Assertion failed")
        }

        return ContentResolver.SCHEME_ANDROID_RESOURCE +
                File.pathSeparator + File.separator + File.separator +
                context.packageName + File.separator + id
    }


    private fun parseLoop(context: Context, parser: XmlResourceParser): Loop {
        var id: String? = null
        var title: String? = null
        var uri: String? = null

        for (i in 0 until parser.attributeCount)
            when (parser.getAttributeName(i)) {
                "id" -> id = parser.getAttributeValue(i)
                "title" -> title = parser.getAttributeValue(i)
            }

        while(parser.next() != XmlPullParser.END_TAG) {
            if(parser.eventType == XmlPullParser.START_TAG) {
                when(parser.name) {
                    "source" -> {
                        uri = parseSource(context, parser)
                    }
                }
            }
        }

        return Loop(id = id!!, title = title!!, uri = uri!!, type = Loop.Type.STATIC)
    }


    private fun parseLoops(context: Context, parser: XmlResourceParser): Iterable<Loop> {
        return mutableListOf<Loop>().apply {
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "loop" ->
                            add(parseLoop(context, parser))
                    }
                }
            }
        }
    }


    fun inflate(context: Context): Iterable<Loop> {
//        val s = System.nanoTime()
//        val res =  parseLoops(context.resources.getXml(R.xml.loops))
//        Timber.d("inflation time: ${(System.nanoTime() - s) / 1e6f}ms")
//        return res

        return parseLoops(context, context.resources.getXml(R.xml.loops))
    }
}