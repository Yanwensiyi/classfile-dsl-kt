import io.github.yanwensiyi.classfile.util.builds
import io.github.yanwensiyi.classfile.util.`class`
import io.github.yanwensiyi.classfile.util.defaultConstructor
import io.github.yanwensiyi.classfile.util.extends
import io.github.yanwensiyi.classfile.util.implements
import io.github.yanwensiyi.classfile.util.method

interface Add {
    fun add(a: Int, b: Int): Int
}

fun main() {
    val clazz = `class`("Test") extends "java.lang.Object" implements Add::class builds {
        defaultConstructor()
        method("add", "(II)I") {
            iload(1)
            iload(2)
            iadd()
            ireturn()
        }
    }
    clazz.getDeclaredMethod("add", Int::class.java, Int::class.java)
        .invoke(clazz.newInstance(), 114514, 1919810)
        .let(::println)
}

