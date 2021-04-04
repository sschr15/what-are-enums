@file:Suppress("unused")

package sschr15.fabricmods.concern.whatareenums

fun <T : Enum<T>> createNewEnum(enum: Class<T>, name: String, vararg parameters: Any?): T {
    val constructor = enum.constructors[0]
    val ordinal = getLastOrdinal(enum) + 1
    ordinalMap[enum] = ordinalMap[enum]!! + 1
    val t = constructor.newInstance(name, ordinal,  *parameters)
    @Suppress("UNCHECKED_CAST")
    return t as T
}

private fun getLastOrdinal(enum: Class<out Enum<*>>) = ordinalMap.getOrPut(enum) {
    enum.fields.filter { it.type == enum }.map { (it.get(null) as Enum<*>).ordinal }.maxOrNull()!!
}

private val ordinalMap = mutableMapOf<Class<out Enum<*>>, Int>()
