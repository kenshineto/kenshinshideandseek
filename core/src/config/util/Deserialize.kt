package cat.freya.khs.config.util

import cat.freya.khs.config.LocaleString1
import cat.freya.khs.config.LocaleString2
import cat.freya.khs.config.LocaleString3
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import org.yaml.snakeyaml.Yaml

fun <T : Any> deserializeClass(type: KClass<T>, data: Map<String, Any?>): T {
    require(type.isData) { "$type is not a data class" }

    val props = type.memberProperties.associateBy { it.name }

    val propValues =
        type.primaryConstructor!!
            .parameters
            .map { props[it.name]!! }
            .associateWith { prop ->
                val value = data[prop.name]
                val propType = prop.returnType.classifier as KClass<*>
                val innerTypes =
                    prop.returnType.arguments.mapNotNull { it.type?.classifier as? KClass<*> }

                // allow null if type is null
                if (prop.returnType.isMarkedNullable && value == null) return@associateWith null

                deserializeField(propType, innerTypes, prop.name, value)
            }

    val instance = type.createInstance()
    for ((prop, value) in propValues) {
        if (value == null && !prop.returnType.isMarkedNullable) error("${prop.name} cannot be null")

        (prop as? KMutableProperty1<*, *>)?.setter?.call(instance, value)
            ?: error("${prop.name} is not mutable")
    }

    val migrateFunction = instance::class.declaredFunctions.singleOrNull { it.name == "migrate" }
    migrateFunction?.call(instance)

    return instance
}

fun <T : Enum<*>> deserializeEnum(type: KClass<T>, key: String, value: String): T {
    return type.java.enumConstants.firstOrNull { it.name == value }
        ?: error("$key: invalid enum value of '$value'")
}

fun <T : Any> deserializeList(innerType: KClass<T>, key: String, value: List<*>): List<T> {
    return value.map { deserializeField(innerType, null, key, it) }
}

fun <K : Any, V : Any> deserializeMap(
    keyType: KClass<K>,
    valueType: KClass<V>,
    key: String,
    value: Map<*, *>,
): Map<String, V> {
    if (keyType != String::class) error("maps may only contain strings as keys")

    return value
        .mapKeys { deserializePrimitive(key, String::class, it.key ?: "") }
        .mapValues { deserializeField(valueType, null, key, it.value) }
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> deserializePrimitive(key: String, expected: KClass<T>, value: Any): T {
    return when (expected) {
        String::class if value is String -> value as T
        LocaleString1::class if value is String -> LocaleString1(value) as T
        LocaleString2::class if value is String -> LocaleString2(value) as T
        LocaleString3::class if value is String -> LocaleString3(value) as T
        Int::class if value is Number -> value.toInt() as T
        UInt::class if value is Number -> maxOf(0, value.toInt()).toUInt() as T
        Long::class if value is Number -> value.toLong() as T
        ULong::class if value is Number -> maxOf(0L, value.toLong()).toULong() as T
        Float::class if value is Number -> value.toFloat() as T
        Double::class if value is Number -> value.toDouble() as T
        Boolean::class if value is Boolean -> value as T
        Boolean::class if value is Number -> (value.toInt() != 0) as T
        else -> error("$key: invalid value '$value' for type $expected")
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> deserializeField(
    type: KClass<T>,
    innerTypes: List<KClass<*>>?,
    key: String,
    value: Any?,
): T {
    return when {
        type.isData ->
            deserializeClass(
                type,
                value as? Map<String, Any?>
                    ?: error("$key: expected map for data class $type, got $value"),
            )

        type.java.isEnum ->
            deserializeEnum(
                type as KClass<Enum<*>>,
                key,
                value as? String ?: error("$key: expected string for enum value, got $value"),
            )
                as T

        type.isSubclassOf(List::class) ->
            deserializeList(
                innerTypes?.firstOrNull() ?: error("$key: innerType not set"),
                key,
                value as? List<*> ?: error("$key: expected list for type $type, got $value"),
            )
                as T

        type.isSubclassOf(Map::class) ->
            deserializeMap(
                innerTypes?.firstOrNull() ?: error("key type not set"),
                innerTypes.getOrNull(1) ?: error("value type not set"),
                key,
                value as? Map<*, *> ?: error("$key: expected map for type $type, got $value"),
            )
                as T

        else -> deserializePrimitive(key, type, value ?: error("$key: value cannot be null"))
    }
}

fun <T : Any> deserialize(type: KClass<T>, ins: InputStream?): T {
    val reader = ins?.let { InputStreamReader(it) } ?: return type.createInstance()
    return deserialize(type, reader)
}

fun <T : Any> deserialize(type: KClass<T>, ins: Reader): T {
    return deserializeClass(type, Yaml().load(ins))
}
