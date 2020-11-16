package com.jack.nars.waver.utility

import android.util.AttributeSet
import android.util.Xml
import org.xmlpull.v1.XmlPullParser


open class XmlParser<P : XmlPullParser>(private val parser: P) {
    protected val attributeSet = Xml.asAttributeSet(parser)

    protected open fun parseArgs(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        for (i in 0 until parser.attributeCount) {
            map[parser.getAttributeName(i)] = parser.getAttributeValue(i)
        }

        return map
    }


    private fun parse(map: Map<String, (Map<String, String>) -> Unit>) {
        while (true) {
            parser.next()

            if (parser.eventType == XmlPullParser.END_TAG) return

            // skip start_document event
            if (parser.eventType == XmlPullParser.START_DOCUMENT) {
                parser.next()

                // skip root if not in child nodes map
                if (parser.name !in map.keys)
                    parser.next()
            }

            if (parser.eventType == XmlPullParser.START_TAG) {
                val tagName = parser.name
                val tagArgs = parseArgs()
                val startDepth = parser.depth

                val toRun = map[tagName]

                toRun?.invoke(tagArgs)

                while (!(parser.eventType == XmlPullParser.END_TAG && parser.depth == startDepth)) {
                    if (parser.eventType == XmlPullParser.END_DOCUMENT) return
                    parser.next()
                }
            }

            if (parser.eventType == XmlPullParser.END_DOCUMENT) break
        }
    }


    fun parse(vararg nodes: Pair<String, (Map<String, String>) -> Unit>) {
        parse(nodes.toMap())
    }
}


class AttributeMap : LinkedHashMap<String, String>() {
    fun getAsResource() {}
}