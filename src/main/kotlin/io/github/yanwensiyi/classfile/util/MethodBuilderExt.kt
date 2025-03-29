package io.github.yanwensiyi.classfile.util

import java.lang.classfile.ClassBuilder
import java.lang.classfile.CodeBuilder

inline val CodeBuilder.load_this: CodeBuilder
    get() = aload(0)

inline fun CodeBuilder.invokeObjectConstructor() {
    load_this
    invokespecial("java.lang.Object", "<init>", "()V")
    return_()
}

inline fun CodeBuilder.invokevirtual(owner: String, name: String, type: String) {
    invokevirtual(owner.toClassDesc(), name, type.toMethodTypeDesc())
}

inline fun CodeBuilder.invokespecial(owner: String, name: String, type: String) {
    invokespecial(owner.toClassDesc(), name, type.toMethodTypeDesc())
}

inline fun ClassBuilder.defaultConstructor(flags: Int = PUBLIC) {
    method("<init>", "()V", flags) {
        invokeObjectConstructor()
    }
}