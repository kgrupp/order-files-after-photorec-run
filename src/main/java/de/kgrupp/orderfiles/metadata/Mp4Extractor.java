package de.kgrupp.orderfiles.metadata;

import de.kgrupp.orderfiles.metadata.model.Mp4Data;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp4.MP4Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Mp4Extractor {

    public static Optional<Mp4Data> extract(File file) {
        try {
            try (InputStream input = new FileInputStream(file)) {
                ContentHandler handler = new DefaultHandler();
                Metadata metadata = new Metadata();
                Parser parser = new MP4Parser();
                ParseContext parseCtx = new ParseContext();
                parser.parse(input, handler, metadata, parseCtx);
                var releaseDateStr = metadata.get("xmpDM:releaseDate");
                if (releaseDateStr != null) {
                    var releaseDate = Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX").withZone(ZoneId.of("Europe/Berlin")).parse(releaseDateStr));
                    return Optional.of(new Mp4Data(releaseDate));
                } else {
                    releaseDateStr = metadata.get("dcterms:created");
                    if (releaseDateStr != null) {
                        var releaseDate = Instant.from(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX").withZone(ZoneId.of("Europe/Berlin")).parse(releaseDateStr));
                        return Optional.of(new Mp4Data(releaseDate));
                    }
                }
                return Optional.empty();
            }
        } catch (IOException | TikaException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
