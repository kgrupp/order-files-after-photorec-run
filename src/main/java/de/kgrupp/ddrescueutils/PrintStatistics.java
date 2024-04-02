package de.kgrupp.ddrescueutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrintStatistics {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Needs arguments: [DDRESCUE-LOG-FILE-PATH]");
        }
        var file = new File(args[0]);
        var content = Files.readString(file.toPath());
        var lines = content.split("\n");
        var filteredLines = Arrays.stream(lines).filter((line) -> line.matches("0x[0-9A-F]+ +(0x[0-9A-F]+) +[+?*/-]")).toList();
        var nonTriedBlocks = getStatistics(filteredLines, "?");
        var failedNonTrimmedBlocks = getStatistics(filteredLines, "*");
        var failedNonScrapedBlocks = getStatistics(filteredLines, "/");
        var failedBadSectorBlocks = getStatistics(filteredLines, "-");
        var rescuedBlocks = getStatistics(filteredLines, "+");
        var total = nonTriedBlocks.getSum() + failedNonTrimmedBlocks.getSum() + failedNonScrapedBlocks.getSum() + failedBadSectorBlocks.getSum() + rescuedBlocks.getSum();
        print("non tried blocks", nonTriedBlocks, total);
        System.out.print("big blocks:\n");
        extractValues(filteredLines, "?").filter(block -> 1024 * 1024 * 50L < block.sizeInBytes ).sorted().forEach(block -> System.out.printf("\t%13s: %s\n", block.position, toReadableFormat(block.sizeInBytes)));
        print("failed non trimmed blocks", failedNonTrimmedBlocks, total);
        System.out.print("big blocks:\n");
        extractValues(filteredLines, "*").filter(block -> 1024 * 1024 * 50L < block.sizeInBytes ).sorted().forEach(block -> System.out.printf("\t%13s: %s\n", block.position, toReadableFormat(block.sizeInBytes)));
        print("failed non scraped blocks", failedNonScrapedBlocks, total);
        System.out.print("big blocks:\n");
        extractValues(filteredLines, "/").filter(block -> 1024 * 1024 * 50L < block.sizeInBytes ).sorted().forEach(block -> System.out.printf("\t%13s: %s\n", block.position, toReadableFormat(block.sizeInBytes)));
        print("failed bad sector blocks", failedBadSectorBlocks, total);
        System.out.print("big blocks:\n");
        extractValues(filteredLines, "-").filter(block -> 1024 * 1024 * 50L < block.sizeInBytes ).sorted().forEach(block -> System.out.printf("\t%13s: %s\n", block.position, toReadableFormat(block.sizeInBytes)));
        print("rescued blocks", rescuedBlocks, total);
        System.out.print("big blocks:\n");
        extractValues(filteredLines, "+").filter(block -> 1024 * 1024 * 50L < block.sizeInBytes ).sorted().forEach(block -> System.out.printf("\t%13s: %s\n", block.position, toReadableFormat(block.sizeInBytes)));
    }

    public static DoubleSummaryStatistics getStatistics(List<String> filteredLines, String status) {
        return extractValues(filteredLines, status).map(Block::sizeInBytes).collect(Collectors.summarizingDouble(Long::doubleValue));
    }

    public static Stream<Block> extractValues(List<String> filteredLines, String status) {
        return filteredLines.stream().filter(line -> line.contains(status)).map(
                line -> {
                    var value = line.replaceFirst("(0x[0-9A-F]+) +0x.*", "$1");
                    var hexNumber = line.replaceFirst("0x[0-9A-F]+ +0x", "").replaceFirst(" +", "").replace(status, "");
                    return new Block(value, Long.parseLong(hexNumber.toLowerCase(), 16));
                }
        );
    }

    public static void print(String statsType, DoubleSummaryStatistics statistics, double total) {
        System.out.printf("\n%s: # %d\n", statsType, statistics.getCount());
        System.out.printf("  sum: %s", toReadableFormat(statistics.getSum()));
        System.out.printf(" percentage: %7.3f \n", statistics.getSum() / total * 100);
        System.out.printf("  min: %s", toReadableFormat(statistics.getMin()));
        System.out.printf("        max: %s\n", toReadableFormat(statistics.getMax()));
        System.out.printf("  avg: %s\n", toReadableFormat(statistics.getAverage()));
    }

    public static String toReadableFormat(double number) {
        if (Double.isFinite(number)) {
            String unit = "B";
            String units = "KMGT";
            double value = number;
            while (value > 1000) {
                value /= 1024;
                unit = units.charAt(0) + "B";
                units = units.substring(1);
            }
            return String.format("%5.1f %-2s", value, unit);
        } else {
            return "  NaN";
        }
    }

    public record Block(String position, Long sizeInBytes) implements Comparable<Block> {

        @Override
            public int compareTo(Block o) {
                return sizeInBytes.compareTo(o.sizeInBytes);
            }
        }
}