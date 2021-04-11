@file:Suppress("unused")

package sschr15.fabricmods.concern.whatareenums

internal fun <T : Enum<T>> createNewEnum(enum: Class<T>, name: String, vararg parameters: Any?): T {
    val constructor = enum.constructors[0]
    @Suppress("UNCHECKED_CAST")
    val values = enum.getField("ENUM_DELETER_VALUES").get(null) as MutableList<T>
    val ordinal = values.maxOf { it.ordinal } + 1
    @Suppress("UNCHECKED_CAST")
    val t = constructor.newInstance(name, ordinal,  *parameters) as T

    values.add(t)
    return t
}
