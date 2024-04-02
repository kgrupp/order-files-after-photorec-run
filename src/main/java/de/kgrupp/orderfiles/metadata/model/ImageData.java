package de.kgrupp.orderfiles.metadata.model;

import java.time.Instant;

public record ImageData(long width, long height, Instant dateTimeOriginal) {

}
