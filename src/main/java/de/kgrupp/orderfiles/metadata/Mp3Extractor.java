package de.kgrupp.orderfiles.metadata;

import de.kgrupp.orderfiles.metadata.model.Mp3Data;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Mp3Extractor {

    public static Optional<Mp3Data> extract(File file) {
        try {
            try (InputStream input = new FileInputStream(file)) {
                ContentHandler handler = new DefaultHandler();
                Metadata metadata = new Metadata();
                Parser parser = new Mp3Parser();
                ParseContext parseCtx = new ParseContext();
                parser.parse(input, handler, metadata, parseCtx);
                var artist = metadata.get("xmpDM:artist");
                var title = metadata.get("dc:title");
                return Optional.of(new Mp3Data(artist, title));
            }
        } catch (IOException | TikaException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
