package de.kgrupp.orderfiles;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class FileFinder {

    private final File location;

    public FileFinder(File location) {
        this.location = location;
    }

    public Stream<File> streamFiles() {
        return getChildren(location).flatMap(file -> {
            if (file.isDirectory()) {
                return getChildren(file);
            } else {
                return Stream.of(file);
            }
        });
    }

    private static Stream<File> getChildren(File file) {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("A directory can not have children");
        }
        var files = file.listFiles();
        if (files == null) {
            return Stream.empty();
        } else {
            return Arrays.stream(files);
        }
    }
}
