import io.github.yanwensiyi.classfile.util.loads
import io.github.yanwensiyi.classfile.util.`class`
import io.github.yanwensiyi.classfile.util.defaultConstructor
import io.github.yanwensiyi.classfile.util.extends
import io.github.yanwensiyi.classfile.util.implements
import io.github.yanwensiyi.classfile.util.method

interface Add {
    fun add(a: Int, b: Int): Int
}

fun main() {
    val clazz = `class`("Test") extends "java.lang.Object" implements Add::class loads {
        defaultConstructor()
        method("add", "(II)I") {
            iload(1)
            iload(2)
            iadd()
            ireturn()
        }
    }
    println((clazz.newInstance() as Add).add(114514, 1919810))
}

