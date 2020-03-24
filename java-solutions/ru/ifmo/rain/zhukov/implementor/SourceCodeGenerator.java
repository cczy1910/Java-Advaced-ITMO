package ru.ifmo.rain.zhukov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility for to {@link Implementor}. Provides tools for source code generation.
 */
public class SourceCodeGenerator {
    /**
     * Class to create implementation for.
     */
    Class<?> token;
    /**
     * Builder for source code as string.
     */
    StringBuilder result = new StringBuilder();
    /**
     * Space token.
     */
    private final String SPACE = " ";
    /**
     * Tabulation token.
     */
    private final String TAB = "    ";
    /**
     * New line token.
     */
    private final String NEWLINE = "\n";

    /**
     * Generator constructor for specified {@code token}
     */
    public SourceCodeGenerator(Class<?> token) {
        this.token = token;
    }

    /**
     * Appends args {@link String} to result {@link StringBuilder}.
     *
     * @param args {@link String} array to append
     */
    private void append(String... args) {
        for (String s : args) {
            result.append(s);
        }
    }

    /**
     * Generates complete source code of token.
     *
     * @return {@link String} containing complete generated source code
     * @throws ImplerException In case non-private constructors are missing
     * @see #generateClassHeader() Class header generation method
     * @see #generateExecutable(String, String, Executable) Executable generation method
     * @see #generateMethods(Method[], Set) Methods generation method
     * @see #translateUnicode(String) Unicode characters translator
     */
    public String generate() throws ImplerException {
        generateClassHeader();
        boolean hasNonPrivateConstuctor = false;
        if (!token.isInterface()) {
            for (Constructor<?> c : token.getDeclaredConstructors()) {
                if (!Modifier.isPrivate(c.getModifiers())) {
                    hasNonPrivateConstuctor = true;
                    generateExecutable(token.getSimpleName(), "", c);
                }
            }
            if (!hasNonPrivateConstuctor) {
                throw new ImplerException("Non-private constructor required");
            }
        }
        Set<EqualityComparableMethod> generatedMethods = new HashSet<>();
        generateMethods(token.getMethods(), generatedMethods);
        for (Class<?> parent = token; parent != null; parent = parent.getSuperclass()) {
            generateMethods(parent.getDeclaredMethods(), generatedMethods);
        }
        if (!token.isInterface() || !generatedMethods.isEmpty()) {
            result.deleteCharAt(result.length() - 1);
        }
        append("}");
        return translateUnicode(result.toString());
    }

    /**
     * Translates input {@link String} characters into universal unicode representation.
     *
     * @param input {@link String} to be translated
     * @return Translation result
     */
    private static String translateUnicode(String input) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : input.toCharArray()) {
            stringBuilder.append(c < 128 ? String.valueOf(c) : String.format("\\u%04x", (int) c));
        }
        return stringBuilder.toString();
    }

    /**
     * Generates given methods.
     *
     * @param methods   {@link Method} to generate.
     * @param generated already generated methods.
     */
    private void generateMethods(Method[] methods, Set<EqualityComparableMethod> generated) {
        for (Method m : methods) {
            if (Modifier.isAbstract(m.getModifiers())) {
                EqualityComparableMethod currentMethod = new EqualityComparableMethod(m);
                if (!generated.contains(currentMethod)) {
                    generated.add(currentMethod);
                    generateExecutable(m.getReturnType().getCanonicalName(), m.getName(), m);
                }
            }
        }
    }

    /**
     * Generates executable.
     *
     * @param type       {@link String} view of Executable return type.
     * @param name       {@link String} view of Executable name.
     * @param executable {@link Executable} to generate.
     */
    private void generateExecutable(String type, String name, Executable executable) {
        append(
                TAB,
                Modifier.toString(executable.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.NATIVE & ~Modifier.TRANSIENT),
                SPACE,
                type,
                name.isEmpty() ? "Impl" : SPACE + name,
                "("
        );
        generateParameters(executable.getParameters());
        append(
                ")",
                SPACE);
        generateTrows(executable.getExceptionTypes());
        append(
                "{",
                NEWLINE,
                TAB,
                TAB);
        if (executable instanceof Method) {
            append(
                    "return",
                    getDefaultValue(((Method) executable).getReturnType()));
        } else {
            append(
                    "super",
                    "(");
            generateSuperArgs(executable.getParameterCount());
            append(
                    ")");
        }
        append(
                ";",
                NEWLINE,
                TAB,
                "}",
                NEWLINE,
                NEWLINE
        );
    }

    /**
     * Generates throws signature.
     *
     * @param exceptionTypes Types of thrown exceptions.
     */
    private void generateTrows(Class<?>[] exceptionTypes) {
        if (exceptionTypes.length != 0) {
            append("throws ");
            for (Class<?> c : exceptionTypes) {
                append(
                        c.getCanonicalName(),
                        ",",
                        SPACE
                );
            }
            result.deleteCharAt(result.length() - 2);
        }
    }

    /**
     * Generates parameters signature.
     *
     * @param parameters list of {@link Parameter}.
     */
    private void generateParameters(Parameter[] parameters) {
        for (Parameter p : parameters) {
            append(
                    p.getType().getCanonicalName(),
                    SPACE,
                    p.getName(),
                    ",",
                    SPACE
            );
        }
        if (parameters.length != 0) {
            result.delete(result.length() - 2, result.length());
        }
    }

    /**
     * Generates arguments for super constructor.
     *
     * @param n number of arguments.
     */
    private void generateSuperArgs(int n) {
        for (int i = 0; i < n; i++) {
            append("arg",
                    Integer.toString(i),
                    ", ");
        }
        if (n > 0) {
            result.delete(result.length() - 2, result.length());
        }
    }


    /**
     * Generates header for implemented class.
     */
    private void generateClassHeader() {
        if (!token.getPackageName().equals("")) {
            append(
                    "package",
                    SPACE,
                    token.getPackageName(),
                    ";",
                    NEWLINE,
                    NEWLINE);
        }
        append(
                "public",
                SPACE,
                "class",
                SPACE,
                token.getSimpleName(),
                "Impl",
                SPACE,
                token.isInterface() ? "implements" : "extends",
                SPACE,
                token.getCanonicalName(),
                SPACE,
                "{",
                NEWLINE
        );
    }

    /**
     * Returns default value for given type.
     */
    private String getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (boolean.class.equals(type)) {
                return " false";
            }
            if (void.class.equals(type)) {
                return "";
            }
            return " 0";
        }
        return " null";
    }

    /**
     * Container for method. Allows to compare methods equality bu signature.
     */
    private static class EqualityComparableMethod {
        Method method;

        EqualityComparableMethod(Method method) {
            this.method = method;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EqualityComparableMethod that = (EqualityComparableMethod) o;
            return this.method.getReturnType().equals(that.method.getReturnType()) &&
                    this.method.getName().equals(that.method.getName()) &&
                    Arrays.equals(this.method.getParameterTypes(), that.method.getParameterTypes());
        }

        @Override
        public int hashCode() {
            int prime = 100000007;
            int result = 1;
            result = result * prime + method.getReturnType().hashCode();
            result = result * prime + method.getName().hashCode();
            result = result * prime + Arrays.hashCode(method.getParameterTypes());
            return result;
        }
    }
}
