package ru.ifmo.rain.zhukov.walk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileVisitor extends SimpleFileVisitor<Path> {
    BufferedWriter writer;

    FileVisitor(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        InputStream input = Files.newInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        int hash = 0x811c9dc5;
        int p = 0x01000193;

        while ((bytesRead = input.read(buffer)) >= 0) {
            for (int i = 0; i < bytesRead; i++) {
                hash = (hash * p) ^ (buffer[i] & 0xff);
            }
        }
        writer.write(String.format("%08x", hash) + " " + file.toString());
        writer.newLine();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        writer.write(String.format("%08x", 0) + " " + file);
        writer.newLine();
        writer.flush();
        return CONTINUE;
    }
}
