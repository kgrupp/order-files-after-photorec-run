package de.kgrupp.orderfiles.model;

import java.util.Arrays;
import java.util.List;

public enum Category {
    IMAGE("jpg", "png", "bmp", "rw2", "tif"),
    VIDEO("mp4", "m2ts", "avi", "3gp", "mov"),
    SOUND("mp3", "wav", "m4p", "wma", "amr"),
    DOCUMENT("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "odp", "ods", "wps", "rtf", "txt"),
    OTHER;

    private final List<String> fileEndings;

    Category(String... fileEndings) {
        this.fileEndings = Arrays.asList(fileEndings);
    }

    static Category getCategory(String fileEnding) {
        return Arrays.stream(Category.values())
                .filter(category -> category.fileEndings.contains(fileEnding))
                .findFirst().orElse(OTHER);
    }
}
