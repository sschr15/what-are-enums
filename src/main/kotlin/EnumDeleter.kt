@file:Suppress("unused")

package sschr15.fabricmods.concern.whatareenums

import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.koffee
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi
import net.fabricmc.loader.api.FabricLoader
import net.khasm.KhasmLoad
import net.khasm.transform.`class`.KhasmClassTransformerDispatcher
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import sschr15.tools.betterpaths.createDirectories
import sschr15.tools.betterpaths.div
import sschr15.tools.betterpaths.write

object EnumDeleter : KhasmLoad {
    override fun loadTransformers() {
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
            }
        }
    }
}
