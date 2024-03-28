package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor


@Target(AnnotationTarget.PROPERTY)
annotation class MapProp(val destName: String)

/**
 * A YamlParser that uses reflection to parse objects.
 */
class YamlParserReflect<T : Any>(type: KClass<T>) : AbstractYamlParser<T>(type) {
    companion object {
        /**
         *Internal cache of YamlParserReflect instances.
         */
        private val yamlParsers: MutableMap<KClass<*>, YamlParserReflect<*>> = mutableMapOf()
        /**
         * Creates a YamlParser for the given type using reflection if it does not already exist.
         * Keep it in an internal cache of YamlParserReflect instances.
         */
        fun <T : Any> yamlParser(type: KClass<T>): AbstractYamlParser<T> {
            return yamlParsers.getOrPut(type) { YamlParserReflect(type) } as YamlParserReflect<T>
        }
    }
    /**
     * Used to get a parser for other Type using the same parsing approach.
     */
    override fun <T : Any> yamlParser(type: KClass<T>) = YamlParserReflect.yamlParser(type)
    /**
     * Creates a new instance of T through the first constructor
     * that has all the mandatory parameters in the map and optional parameters for the rest.
     */
    override fun newInstance(args: Map<String, Any>): T {
        val ctor = type.constructors.first() //tem que criar instancia de T, nao de YamlParserReflect

        val map: Map<KParameter, Any?> = this::class
            .memberProperties
            .map { fromProp ->
                    fromProp to matchParameter(fromProp, ctor.parameters) }
            .filter{ it.second != null }
            .associate { pair ->
                val fromVal = pair.first.call(this)
                val destArg = pair.second!!       
                destArg to fromVal }
        return ctor.callBy(map)

    }

}
fun matchParameter(
    srcProp: KProperty<*>,
    ctorParameters: List<KParameter>) : KParameter?{
    return ctorParameters.firstOrNull { arg ->
        srcProp.returnType == arg.type
                && hasSameName(srcProp, arg)
    }
}

fun hasSameName(srcProp: KProperty<*>, arg: KParameter): Boolean {
    if(srcProp.name == arg.name)
        return true
    val annot = srcProp
        .findAnnotation<MapProp>()
        ?: return false
    return annot.destName == arg.name
}