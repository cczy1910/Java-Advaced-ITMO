/**
 * {@link info.kgeorgiy.java.advanced.implementor.Impler} and
 * {@link info.kgeorgiy.java.advanced.implementor.JarImpler} implementation and
 * utilities for source code generation.
 */
module ru.ifmo.rain.zhukov {
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.base;
    requires java.compiler;

    opens ru.ifmo.rain.zhukov.implementor;
    exports ru.ifmo.rain.zhukov.implementor;
}