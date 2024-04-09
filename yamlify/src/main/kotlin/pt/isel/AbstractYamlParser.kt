package pt.isel

import java.io.Reader
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

abstract class AbstractYamlParser<T : Any>(val type: KClass<T>) : YamlParser<T> {
    /**
     * Used to get a parser for other Type using this same parsing approach.
     */
    abstract fun <T : Any> yamlParser(type: KClass<T>): AbstractYamlParser<T>

    /**
     * Creates a new instance of T through the first constructor
     * that has all the mandatory parameters in the map and optional parameters for the rest.
     */
    abstract fun newInstance(args: Map<String, Any>): T


    final override fun parseObject(yaml: Reader): T {
        val props = type.memberProperties
        val map: MutableMap<String, Any> = mutableMapOf()
        val text = yaml.readText()
        val tlist = text.split("\n")
        val list = arrayListOf<List<String>>()
        for (i in tlist.indices) {
            val objlist = arrayListOf<String>()
            if (tlist[i] != "") {
                if (tlist[i][16] != ' ') {
                    val pair = tlist[i].split(": ")
                    if (pair.size == 1) {
                        objlist.addLast(pair[0])
                        for (j in i..<tlist.size) {
                            if (tlist[j][16] == ' ') {
                                objlist.addLast(tlist[j])
                            }
                        }
                        list.addLast(objlist)
                    } else {
                        list.addLast(listOf(tlist[i]))
                    }
                }

            }
        }

        list.forEach { lineList ->

            if (lineList.size == 1) {
                val l = lineList[0].split(": ")
                val key = l[0].trim()
                props.forEach { prop ->
                    if (prop.name == key) {
                        map[key] = l[1]
                    }
                }
            } else {
                val key = lineList[0].trim().split(':')[0]
                map[key] = parseObj(lineList)

            }
        }

        return newInstance(map)
    }

    fun parseObj(l: List<String>): MutableMap<String, Any> {
        val map: MutableMap<String, Any> = mutableMapOf()
        l.forEach { line ->
            val pair = line.trim().split(':')
            val key = pair[0].trim()
            if (pair[1] != "") {
                map[key] = pair[1]
            }
        }

        return map
    }


    final override fun parseList(yaml: Reader): List<T> {
        val props = type.memberProperties
        val map: Map<String, Any> = mapOf()
        // val objString = yaml.toString()
        yaml.forEachLine { line ->
            val a = line.split("-")
            props.forEach { propName ->
                if (propName.name == a[0]) {
                    map.plus(Pair(propName.name, a[1]))
                }
            }
        }
        val i = listOf(newInstance(map))
        return i
    }


}
