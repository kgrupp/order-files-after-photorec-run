package de.kgrupp.orderfiles.metadata;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import de.kgrupp.orderfiles.metadata.model.ImageData;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JpgExtractor {

    public static Optional<ImageData> extract(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            var width = extractLong(metadata, JpegDirectory.TAG_IMAGE_WIDTH);
            var height = extractLong(metadata, JpegDirectory.TAG_IMAGE_HEIGHT);
            if (width != null && height != null) {
                var instant = extractInstant(metadata, ExifIFD0Directory.TAG_DATETIME_ORIGINAL);
                return Optional.of(new ImageData(width, height, instant));
            } else {
                return Optional.empty();
            }
        } catch (IOException | ImageProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Long extractLong(Metadata metadata, int exifSubIfdirectoryId) {
        return toStream(metadata.getDirectories().iterator())
                .filter(directory -> directory.containsTag(exifSubIfdirectoryId))
                .map(directory -> {
                    try {
                        return directory.getLong(exifSubIfdirectoryId);
                    } catch (MetadataException e) {
                        throw new RuntimeException(e);
                    }
                }).findFirst().orElse(null);
    }

    private static Instant extractInstant(Metadata metadata, int exifSubIfdirectoryId) {
        return toStream(metadata.getDirectories().iterator())
                .filter(directory -> directory.containsTag(exifSubIfdirectoryId))
                .map(directory ->
                        directory.getDate(exifSubIfdirectoryId))
                .filter(Objects::nonNull).findFirst().map(Date::toInstant).orElse(null);
    }

    private static <T> Stream<T> toStream(Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
