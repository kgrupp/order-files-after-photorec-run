package de.kgrupp.orderfiles;

import de.kgrupp.orderfiles.model.FileWithType;
import java.io.File;

public class OrderFilesApplication {

    public static final boolean DRY_RUN = false;

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Needs arguments: [SOURCE] [DESTINATION]");
        }
        var source = new File(args[0]);
        var target = new File(args[1]);
        var fileFinder = new FileFinder(source);
        var reorderFiles = new ReorderFiles(target);
        fileFinder.streamFiles().map(FileWithType::new).forEach(reorderFiles::handleFile);
        reorderFiles.getStatistics().forEach(
                (category, count) -> System.out.println(category + ": " + count)
        );
    }


}
