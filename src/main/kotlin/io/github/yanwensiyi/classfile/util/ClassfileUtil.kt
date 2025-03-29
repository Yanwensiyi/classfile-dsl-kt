package io.github.yanwensiyi.classfile.util

import java.lang.classfile.ClassBuilder
import java.lang.classfile.ClassFile
import java.lang.classfile.CodeBuilder
import java.lang.constant.ClassDesc
import java.lang.constant.MethodTypeDesc
import kotlin.reflect.KClass

fun interface ClassDefiner {
    fun defineClass(bytes: ByteArray, args: Map<*, *>?): Class<*>
}

class DefaultLoader : ClassLoader(), ClassDefiner {
    fun defineClass(bytes: ByteArray) = defineClass(bytes, null)
    override fun defineClass(bytes: ByteArray, args: Map<*, *>?): Class<*> {
        return defineClass(null, bytes, 0, bytes.size)
    }
}

const val PUBLIC = ClassFile.ACC_PUBLIC
const val STATIC = ClassFile.ACC_STATIC

fun String.toClassDesc(): ClassDesc = ClassDesc.of(this)
fun String.toClassDescDescriptor(): ClassDesc = ClassDesc.ofDescriptor(this)
fun String.toClassDescOfArray(rank: Int = 1): ClassDesc = ClassDesc.of(this).arrayType(rank)
fun String.toMethodTypeDesc(): MethodTypeDesc = MethodTypeDesc.ofDescriptor(this)

class Context(
    val classFile: ClassFile,
    val desc: ClassDesc,
    var superclass: String? = null,
    var interfaces: ArrayList<String>? = null,
    var args: Map<*, *>? = null,
    var definer: ClassDefiner? = null
)

inline fun buildClass(
    name: String,
    options: Array<ClassFile.Option>? = null,
    args: Map<*, *>? = null,
    definer: ClassDefiner = DefaultLoader(),
    crossinline block: ClassBuilder.(ClassFile) -> Unit,
) = buildClass(name.toClassDesc(), options, args, definer, block)

inline fun buildClass(
    desc: ClassDesc,
    options: Array<ClassFile.Option>? = null,
    args: Map<*, *>? = null,
    definer: ClassDefiner = DefaultLoader(),
    crossinline block: ClassBuilder.(ClassFile) -> Unit,
): Class<*> {
    val classFile = (if (options != null) ClassFile.of(*options) else ClassFile.of())!!
    return definer.defineClass(classFile.build(desc) {
        it.block(classFile)
    }, args)
}

inline fun `class`(
    name: String,
    options: Array<ClassFile.Option>? = null,
    args: Map<*, *>? = null,
    definer: ClassDefiner = DefaultLoader()
): Context = `class`(name.toClassDesc())

inline fun `class`(
    desc: ClassDesc,
    options: Array<ClassFile.Option>? = null,
    args: Map<*, *>? = null,
    definer: ClassDefiner = DefaultLoader()
): Context =
    Context(if (options != null) ClassFile.of(*options) else ClassFile.of(), desc, args = args, definer = definer)

inline infix fun Context.extends(clazz: KClass<*>) = apply {
    superclass = clazz.qualifiedName!!
}

inline infix fun Context.extends(clazz: Class<*>) = apply {
    superclass = clazz.name
}

inline infix fun Context.extends(clazz: String) = apply {
    superclass = clazz
}

inline infix fun Context.implements(classes: List<*>) = apply {
    interfaces = ArrayList(classes.map {
        when (it) {
            is Class<*> -> it.name
            is KClass<*> -> it.qualifiedName!!
            else -> it.toString()
        }
    })
}

inline infix fun Context.implements(clazz: KClass<*>) = apply {
    if (interfaces == null)
        interfaces = ArrayList()
    interfaces?.add(clazz.qualifiedName!!)
}

inline infix fun Context.implements(clazz: Class<*>) = apply {
    if (interfaces == null)
        interfaces = ArrayList()
    interfaces?.add(clazz.name)
}

inline infix fun Context.implements(clazz: String) = apply {
    if (interfaces == null)
        interfaces = ArrayList()
    interfaces?.add(clazz)
}

infix fun Context.builds(block: ClassBuilder.() -> Unit): ByteArray {
    return classFile.build(desc) {
        val superclass = superclass
        if (superclass != null)
            it.withSuperclass(superclass.toClassDesc())
        val interfaces = interfaces
        if (!interfaces.isNullOrEmpty()) {
            val pool = it.constantPool()
            it.withInterfaces(interfaces.map { pool.classEntry(it.toClassDesc()) })
        }
        it.block()
    }
}

infix fun Context.loads(block: ClassBuilder.() -> Unit): Class<*> {
    return definer!!.defineClass(builds(block), args)
}

inline fun ClassBuilder.implements(vararg types: String) {
    val pool = constantPool()
    withInterfaces(types.map { pool.classEntry(it.toClassDesc()) })
}

inline fun ClassBuilder.implements(vararg types: Class<*>) {
    val pool = constantPool()
    withInterfaces(types.map { pool.classEntry(it.name.toClassDesc()) })
}

inline fun ClassBuilder.method(
    name: String,
    type: String,
    flags: Int = PUBLIC,
    crossinline block: CodeBuilder.() -> Unit
) {
    withMethodBody(name, type.toMethodTypeDesc(), flags) {
        it.block()
    }
}

inline fun ClassBuilder.field(name: String, type: String, flags: Int = PUBLIC) {
    withField(name, type.toClassDesc(), flags)
}

inline fun ClassBuilder.field(name: String, type: KClass<*>, flags: Int = PUBLIC) {
    withField(name, type.qualifiedName!!.toClassDesc(), flags)
}

inline fun ClassBuilder.field(name: String, type: Class<*>, flags: Int = PUBLIC) {
    withField(name, type.name.toClassDesc(), flags)
}