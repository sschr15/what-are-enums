@file:Suppress("unused")

package sschr15.fabricmods.concern.whatareenums

import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.koffee
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi
import net.fabricmc.loader.api.FabricLoader
import net.khasm.KhasmLoad
import net.khasm.transform.`class`.KhasmClassTransformerDispatcher
import net.khasm.transform.method.target.HeadTarget
import net.khasm.transform.method.target.OpcodeTarget
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import sschr15.tools.betterpaths.createDirectories
import sschr15.tools.betterpaths.div
import sschr15.tools.betterpaths.write
import java.util.*

object EnumDeleter : KhasmLoad {
    override fun loadTransformers() {
        // making the Class class more better so it accepts modified enums
        InstrumentationApi.retransform(Class::class.java) { _, node: ClassNode ->
            node.methods.first { it.name == "isEnum" }.apply {
                instructions.clear()
                visitCode()
                koffee {
                    aload_0
                    invokevirtual(Class::class, "getSuperclass", Class::class)
                    ldc(coerceType(java.lang.Enum::class))
                    if_acmpne(L[1])
                    iconst_1
                    goto(L[2])
                    +L[1]
                    iconst_0
                    +L[2]
                    ireturn
                }
                visitEnd()
            }
            (FabricLoader.getInstance().gameDir / "khasm/java/lang")
                .createDirectories()
                .resolve("Class.class")
                .write(ClassWriter(0).also { node.accept(it) }.toByteArray())
        }

        KhasmClassTransformerDispatcher.registerClassTransformer {
            classTarget {
                name.startsWith("net/minecraft") && access and Opcodes.ACC_ENUM != 0
            }

            action {
                node.access = node.access and Opcodes.ACC_ENUM.inv()

                for (field in node.fields) {
                    field.access = field.access and Opcodes.ACC_ENUM.inv()
                }

                val clsnode = node
                val enumDeleterValues = field(
                    public + static + final,
                    "ENUM_DELETER_VALUES",
                    List::class,
                    "Ljava/util/List<L${clsnode.name};>;"
                )

                var originalField = ""

                // Put all the stuffs into a modifiable list instead of an immutable array
                transformMethod {
                    // We have to override the class target because the previous target wouldn't match (we removed the `enum` flag)
                    classTarget { this == clsnode }
                    methodTarget { name == "<clinit>" }
                    target { OpcodeTarget(Opcodes.PUTSTATIC) }
                    action { rawInject {
                        it as FieldInsnNode
                        if (instructions.last.opcode == Opcodes.AASTORE) {
                            originalField = it.name
                            // turn the array into a list
                            invokestatic(Arrays::class, "asList", List::class, Array<Any>::class)
                            astore_0

                            // copying into a fresh list (array-backed lists can't change length)
                            new(ArrayList::class)
                            dup
                            aload_0
                            invokespecial(ArrayList::class, "<init>", void, Collection::class)
                            putstatic(clsnode, enumDeleterValues)

                            // there's still a PUTSTATIC that we need to take care of
                            iconst_0
                            anewarray(clsnode)
                        }
                    } }
                }

                // Use the list instead of the (now empty) array for making the things
                transformMethod {
                    classTarget { this == clsnode }
                    methodTarget { name == "values" && desc == "()[L${clsnode.name};" }
                    target { HeadTarget() }
                    action { rawOverwrite {
                        getstatic(clsnode, enumDeleterValues)
                        getstatic(clsnode, originalField, "[L${clsnode.name};")
                        invokeinterface(List::class, "toArray", Array<Any>::class, Array<Any>::class)
                        checkcast("[L${clsnode.name};")
                        areturn
                    } }
                }
            }
        }

        FabricLoader.getInstance().allMods
            .mapNotNull { it.metadata.customValues["whatareenums"]?.asObject }
            .associate { it["package"]!!.asString to it["classes"]!!.asArray }
            .flatMap { (pkg, cls) -> cls.map { "$pkg.$cls" } }
            .sorted()
            .forEach { Class.forName(it, true, this::class.java.classLoader) }
    }
}
