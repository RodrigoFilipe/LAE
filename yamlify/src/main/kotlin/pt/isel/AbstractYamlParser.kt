package pt.isel

import java.io.Reader
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

abstract class AbstractYamlParser<T : Any>(val type: KClass<T>) : YamlParser<T> {
    /**
     * Used to get a parser for other Type using this same parsing approach.
     */
    abstract fun <T : Any> yamlParser(type: KClass<T>) : AbstractYamlParser<T>
    /**
     * Creates a new instance of T through the first constructor
     * that has all the mandatory parameters in the map and optional parameters for the rest.
     */
    abstract fun newInstance(args: Map<String, Any>): T
/*
nome: Rodrigo
Idade: 20
 */

    final override fun parseObject(yaml: Reader): T {
        val props = type.memberProperties
        val map: Map<String, Any> = mapOf()
        yaml.forEachLine { line ->
            val a = line.split(":")
            props.forEach{
                propName ->
                    if(propName.name == a[0])
                        map.plus(Pair(propName.name, a[1]))}
        }


        val i = newInstance(map)
        return i
    }

    final override fun parseList(yaml: Reader): List<T> {
        val props = type.memberProperties
        val map: Map<String, Any> = mapOf()
        // val objString = yaml.toString()
        val y = yaml.forEachLine { line ->
            val a = line.split("-")
            props.forEach{ propName -> if(propName.name == a[0]) {
                map.plus(Pair(propName.name, a[1]))}
            }
        }
        val i = listOf(newInstance(map))
        return i
    }

}
