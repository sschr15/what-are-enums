@file:Suppress("unused")

package sschr15.fabricmods.concern.whatareenums

import net.fabricmc.loader.api.FabricLoader

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

private val classes: Map<String, List<String>> by lazy {
    val map = mutableMapOf<String, MutableList<String>>()
    FabricLoader.getInstance().allMods
        .mapNotNull { it.metadata.customValues["whatareenums"]?.asObject }
        .filter { it["enums"] != null }
        .associate { it["package"]!! to it["enums"]!!.asObject }
        .forEach { (pkg, enums) ->
            enums.forEach { (enum, classes) ->
                map.computeIfAbsent(enum) { mutableListOf() }
                    .addAll(classes.asArray.map { "$pkg.${it.asString}" })
            }
        }

    // immutability
    map.mapValues { it.value.sorted() }
}

internal lateinit var classLoader: ClassLoader

fun loadExpansions(`class`: Class<out Enum<*>>) {
    val name = `class`.name
    classes[name]?.forEach { Class.forName(it, true, classLoader) }
}
