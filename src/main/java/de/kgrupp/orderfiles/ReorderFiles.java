package de.kgrupp.orderfiles;

import de.kgrupp.orderfiles.metadata.model.ImageData;
import de.kgrupp.orderfiles.metadata.model.Mp4Data;
import de.kgrupp.orderfiles.model.FileWithType;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ReorderFiles {

    public static final int MIN_PIXEL = 25_000;
    private final File destinationFolder;
    private final Map<String, Long> statistics = new HashMap<>();

    public ReorderFiles(File destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public void handleFile(FileWithType file) {
        statistics.merge(file.getFileEnding(), 1L, Long::sum);
        switch (file.getCategory()) {
            case IMAGE -> handleImage(file);
            case VIDEO -> handleVideo(file);
            case SOUND -> handleSound(file);
            case DOCUMENT -> handleDocument(file);
            case OTHER -> handleOther(file);
        }
    }

    private void handleImage(FileWithType file) {
        var imageData = file.getImageData();
        imageData.ifPresent((imageData1) -> {
            if (imageData1.dateTimeOriginal() != null) {
                statistics.merge("original-date-found-in-image", 1L, Long::sum);
            }
        });
        var date = imageData.map(ImageData::dateTimeOriginal).orElse(file.getLastChanged());
        var year = date.atOffset(ZoneOffset.UTC).getYear();
        var destination = destinationFolder.toPath().resolve("images").resolve("" + year).toFile();
        File targetFolder;
        if (imageData.isPresent() && isPreview(imageData.get())) {
            statistics.merge("preview-images", 1L, Long::sum);
            targetFolder = destination.toPath().resolve("previews").toFile();
        } else {
            statistics.merge("relevant-images", 1L, Long::sum);
            targetFolder = destination;
        }
        ensureDirectoryExists(targetFolder);
        file.renameTo(targetFolder, getIsoDateTime(date) + " - " + file.getFileName());
    }

    private boolean isPreview(ImageData imageData) {
        return imageData.height() * imageData.width() < MIN_PIXEL;
    }

    private void handleVideo(FileWithType file) {
        var fileEnding = file.getFileEnding();
        var destination = destinationFolder.toPath().resolve("video").resolve(fileEnding).toFile();
        ensureDirectoryExists(destination);
        var videoData = file.getVideoData();
        var date = videoData.map(Mp4Data::creationDate).orElse(file.getLastChanged());
        file.renameTo(destination, getIsoDateTime(date) + " - " + file.getFileName());
    }

    private void handleSound(FileWithType file) {
        var fileEnding = file.getFileEnding();
        var destination = destinationFolder.toPath().resolve("sound").resolve(fileEnding).toFile();
        ensureDirectoryExists(destination);
        var soundDataOpt = file.getSoundData();
        if (soundDataOpt.isEmpty()) {
            file.renameTo(destination, getIsoDateTime(file.getLastChanged()) + " - " + file.getFileName());
        } else {
            var soundData = soundDataOpt.get();
            var newName = (soundData.artist() + " - " + soundData.title()).replaceAll("[/:\\\\?<>]", "_");
            file.renameTo(destination, newName + " - " + file.getFileName());
        }
    }

    private void handleDocument(FileWithType file) {
        var fileEnding = file.getFileEnding();
        var destination = destinationFolder.toPath().resolve("documents").resolve(fileEnding).toFile();
        ensureDirectoryExists(destination);
        file.renameTo(destination, getIsoDateTime(file.getLastChanged()) + " - " + file.getFileName());
    }

    private void handleOther(FileWithType file) {
        var fileEnding = file.getFileEnding();
        var destination = destinationFolder.toPath().resolve("other-files").resolve(fileEnding).toFile();
        ensureDirectoryExists(destination);
        file.renameTo(destination, getIsoDateTime(file.getLastChanged()) + " - " + file.getFileName());
    }

    private void ensureDirectoryExists(File destinationFolder) {
        if (!destinationFolder.exists()) {
            if (!destinationFolder.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + destinationFolder.getAbsolutePath());
            }
        }
    }

    private String getIsoDateTime(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss.S").withZone(ZoneId.of("Europe/Berlin")).format(instant);
    }

    public Map<String, Long> getStatistics() {
        return statistics;
    }
}
