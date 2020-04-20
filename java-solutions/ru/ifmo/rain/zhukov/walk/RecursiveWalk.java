package ru.ifmo.rain.zhukov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    static void walk(Path inputPath, Path outputPath) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                String file;
                FileVisitor fileVisitor = new FileVisitor(writer);
                try {
                    while ((file = reader.readLine()) != null) {
                        try {
                            Path path = Paths.get(file);
                            Files.walkFileTree(path, fileVisitor);
                        } catch (InvalidPathException | IOException e) {
                            writer.write(String.format("%08x", 0) + " " + file);
                            writer.newLine();
                            writer.flush();
                        }
                    }
                } catch (IOException e) {
                    throw new Exception("Input file reading error: " + e.getMessage());
                }
            } catch (IOException e) {
                throw new Exception("Writer creation error: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new Exception("Reader creation error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Path inputPath;
            Path outputPath;
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new Exception("Wrong arguments");
            }
            try {
                inputPath = Paths.get(args[0]);
            } catch (InvalidPathException e) {
                throw new Exception("Invalid input path: " + e.getMessage());
            }
            try {
                outputPath = Paths.get(args[1]);
            } catch (InvalidPathException e) {
                throw new Exception("Invalid output path: " + e.getMessage());
            }

            if (outputPath.getParent() != null) {
                try {
                    Files.createDirectories(outputPath.getParent());
                } catch (IOException e) {
                    throw new Exception("Directory creation error: " + e.getMessage());
                }
            }

            walk(inputPath, outputPath);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
