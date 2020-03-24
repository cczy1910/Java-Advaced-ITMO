package ru.ifmo.rain.zhukov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Class implementing {@link JarImpler} and extending {@link Implementor}. Adds functionality
 * to create {@code JAR} containing compiled generated implementation of given class.
 */
public class JarImplementor extends Implementor implements JarImpler {
    /**
     * Main function to provide console interface of the program.
     * <p>
     * Allowed signature: {@code [-jar] token outputPath}
     * <p>
     * When {@code -jar} is omitted, the program runs in Implementation mode and
     * {@link info.kgeorgiy.java.advanced.implementor.Impler#implement(Class, Path)} is invoked.
     * <p>
     * When {@code -jar} is used, the program runs in JarImplementation mode and
     * {@link info.kgeorgiy.java.advanced.implementor.JarImpler#implementJar(Class, Path)} is invoked.
     * <p>
     * All arguments must not be null. Errors are printed to {@code STDERR}.
     *
     * @param args Provided to program arguments
     */
    public static void main(String[] args) {
        try {
            if (args != null && (args.length == 2 || args.length == 3) && args[0] != null && args[1] != null) {
                if (args.length == 3 && args[2] == null) {
                    throw new ImplerException("Incorrect arguments");
                }
                boolean jarOption;
                if (args.length == 2) {
                    jarOption = false;
                } else {
                    if ("--jar".equals(args[0])) {
                        jarOption = true;
                        args[0] = args[1];
                        args[1] = args[2];
                    } else {
                        throw new ImplerException("Incorrect arguments");
                    }
                }
                Class<?> token;
                Path root;
                try {
                    token = Class.forName(args[0]);
                    try {
                        root = Paths.get(args[1]);
                        if (jarOption) {
                            new JarImplementor().implementJar(token, root);
                        } else {
                            new Implementor().implement(token, root);
                        }

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

    /**
     * Compiles class, which implements given {@code token} class
     *
     * @param token type token to create implementation for
     * @param path  directory to store compiled file
     * @throws ImplerException if compilation fails for some reason
     */
    private void compile(final Class<?> token, final Path path) throws ImplerException {
        final String file = Path.of(path.toString(),
                token.getPackageName().replace('.',
                        File.separatorChar), token.getSimpleName() + "Impl.java").toString();
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Could not find java compiler");
        }
        Path classPath;
        try {
            classPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new ImplerException("Class path creation error");
        }

        final List<String> args = new ArrayList<>();
        args.add(file);
        args.add("-cp");
        args.add(path + File.pathSeparator + classPath.toString());

        final int exitCode = compiler.run(null, null, null, args.toArray(String[]::new));
        if (exitCode != 0) {
            throw new ImplerException("Implementation compilation error");
        }
    }

    /**
     * Creates an artifact for created {@code .class}-file.
     *
     * @param token   type token for which {@code .jar}-file is generated
     * @param jarPath target for the {@code .jar}-file
     * @param tmpPath path to the compiled implementation class
     * @throws ImplerException when unable to write to the {@code .jar}-file.
     */
    private void generateArtifact(final Class<?> token, final Path jarPath, final Path tmpPath) throws ImplerException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (final JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            final String name = token.getPackageName().replace('.', '/') + "/" + token.getSimpleName() + "Impl.class";
            outputStream.putNextEntry(new ZipEntry(name));
            Files.copy(Paths.get(tmpPath.toString(), name), outputStream);
        } catch (final IOException e) {
            throw new ImplerException(e.getMessage());
        }
    }

    /**
     * Produces {@code .jar} file implementing class or interface specified by provided {@code token}.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target {@code .jar} file.
     * @throws ImplerException when implementation cannot be generated.
     * @see #implement(Class, Path) class implemention method
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        Path tmpDirectory;
        try {
            Path parentDirectory = jarFile.toAbsolutePath().getParent();
            Files.createDirectories(parentDirectory);
            tmpDirectory = Files.createTempDirectory(parentDirectory, "impl-tmp");
        } catch (IOException e) {
            throw new ImplerException("Directories creation error");
        }
        try {
            implement(token, tmpDirectory);
            compile(token, tmpDirectory);
            generateArtifact(token, jarFile, tmpDirectory);
        } finally {
            deleteRecursively(tmpDirectory.toFile());
        }
    }

    /**
     * Deletes directory and all it's contains
     *
     * @param file {@link File} to delete
     * @throws ImplerException when unable to delete directory
     */
    private void deleteRecursively(File file) throws ImplerException {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteRecursively(f);
            }
        }
        if (!file.delete()) {
            throw new ImplerException("Tmp directory deletion error");
        }
    }
}