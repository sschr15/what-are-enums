package sschr15.fabricmods.concern.whatareenums

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class EnumDelegate<T : Enum<T>> internal constructor(private val type: Class<T>, private vararg val parameters: Any?) : ReadOnlyProperty<Any?, T> {
    private lateinit var enumValue: T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!::enumValue.isInitialized) {
            val name = property.name
            enumValue = createNewEnum(type, name, parameters)
        }
        return enumValue
    }
}

fun <T : Enum<T>> enum(type: KClass<T>, vararg parameters: Any?): ReadOnlyProperty<Any?, T> =
    EnumDelegate(type.java, *parameters)
