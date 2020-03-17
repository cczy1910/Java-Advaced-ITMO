package ru.ifmo.rain.zhukov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements Impler {
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {

        if (token == null || root == null) {
            throw new ImplerException("Null argument");
        }

        if (token.isPrimitive() || token.isArray() || token == Enum.class || Modifier.isFinal(token.getModifiers())
                || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Unsupported token");
        }

        Path resultPath;

        try {
            resultPath = Paths.get(root.toString(),
                    (Paths.get(token.getPackageName().replaceAll("\\.", "/")) + "/" + token.getSimpleName() + "Impl.java"));
        } catch (InvalidPathException e) {
            throw new ImplerException("Invalid path");
        }

        if (resultPath.getParent() != null) {
            try {
                Files.createDirectories(resultPath.getParent());
            } catch (IOException e) {
                throw new ImplerException("Directories creation error");
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(resultPath)) {
            SourceCodeGenerator generator = new SourceCodeGenerator(token);
            String result = generator.generate();
            writer.write(result);
        } catch (IOException e) {
            throw new ImplerException("Writing error");
        }
    }

    public static void main(String[] args) {
        try {
            if (args != null && args.length == 2 && args[0] != null && args[1] != null) {
                Class<?> token;
                Path root;
                try {
                    token = Class.forName(args[0]);
                    try {
                        root = Paths.get(args[1]);
                        new Implementor().implement(token, root);
                    } catch (InvalidPathException e) {
                        throw new ImplerException("Invalid output path");
                    }
                } catch (ClassNotFoundException e) {
                    throw new ImplerException("Incorrect token");
                }
            } else {
                throw new ImplerException("Incorrect arguments");
            }
        } catch (ImplerException e) {
            System.err.println(e.getMessage());
        }
    }
}
