package de.kgrupp.orderfiles.model;

import de.kgrupp.orderfiles.metadata.JpgExtractor;
import de.kgrupp.orderfiles.metadata.Mp3Extractor;
import de.kgrupp.orderfiles.metadata.Mp4Extractor;
import de.kgrupp.orderfiles.metadata.model.ImageData;
import de.kgrupp.orderfiles.metadata.model.Mp3Data;
import de.kgrupp.orderfiles.metadata.model.Mp4Data;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.kgrupp.orderfiles.OrderFilesApplication.DRY_RUN;

public record FileWithType(File file) implements FileType {
    private static final Map<File, Integer> duplicatedNames = new HashMap<>();

    @Override
    public Category getCategory() {
        return Category.getCategory(getFileEnding());
    }

    @Override
    public String getFileEnding() {
        return getFileEnding(file);
    }

    public void renameTo(File directory, String fileName) {
        var newPath = handleDuplicates(directory.toPath().resolve(fileName).toFile());
        if (DRY_RUN) {
            System.out.println("renameFile to " + newPath);
        } else {
            if (!file.renameTo(newPath)) {
                throw new IllegalStateException("Could not move file '" + file.getAbsolutePath() + "' to directory " + newPath);
            }
        }
    }

    private static File handleDuplicates(File path) {
        if (path.exists()) {
            var duplicateNumber = duplicatedNames.merge(path, 1, Integer::sum);
            var newPath = new File(path.getAbsolutePath().replaceFirst("(\\.[^./\\\\]+)$", "-duplicate-" + duplicateNumber + "-$1"));
            return handleDuplicates(newPath);
        } else {
            return path;
        }
    }

    public Instant getLastChanged() {
        try {
            var attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return attributes.lastModifiedTime().toInstant();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ImageData> getImageData() {
        if ("jpg".equals(getFileEnding())) {
            return JpgExtractor.extract(file);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Mp3Data> getSoundData() {
        if ("mp3".equals(getFileEnding())) {
            return Mp3Extractor.extract(file);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Mp4Data> getVideoData() {
        if ("mp4".equals(getFileEnding())) {
            return Mp4Extractor.extract(file);
        } else {
            return Optional.empty();
        }
    }

    public String getFileName() {
        return file.getName();
    }

    private static String getFileEnding(File file) {
        return file.getAbsolutePath().replaceFirst(".*\\.([^.\\\\/]+)", "$1");
    }
}
