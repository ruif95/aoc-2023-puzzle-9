import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class Main {
    public static final String WRONG_INPUT_FILE_MESSAGE = "Wrong file";
    public static final String INPUT_FILENAME = "input";

    public static Map<String, AlmanacMap> almanacMaps;

    public static void main(String[] args) throws IOException {
        List<String> inputLines = extractInputLines();
        List<Long> seeds = getSeedsFromFirstInputLine(inputLines.get(0));

        inputLines = inputLines.subList(2, inputLines.size()); // remove initial seed list now that we already have it

        almanacMaps = parseMapsFromInput(inputLines);

        OptionalLong result = seeds.stream()
                                  .mapToLong(seed -> Main.getSeedFinalNumber("seed", seed))
                                  .min();

        if (result.isEmpty()) {
            throw new RuntimeException("Oops! Something wrong.");
        }

        System.out.println("Lowest number is: " + result.getAsLong());
    }

    private static long getSeedFinalNumber(String almanacKey, Long seed) {
        AlmanacMap currentMap = almanacMaps.get(almanacKey);

        if (currentMap == null) {
            return seed;
        }

        return getSeedFinalNumber(currentMap.getDestination(), currentMap.getDestinationNumber(seed));
    }

    private static Map<String, AlmanacMap> parseMapsFromInput(List<String> inputLines) {
        boolean notFirstMap = false;
        String source = "";
        String destination = "";
        List<AlmanacMapping> mappings = new ArrayList<>();

        Map<String, AlmanacMap> maps = new HashMap<>();

        for (String line : inputLines) {
            if (line.isEmpty()) {
                continue;
            }

            if (Character.isLetter(line.charAt(0))) {
                if (notFirstMap) {
                    maps.put(source, new AlmanacMap(source, destination, mappings));
                }

                mappings = new ArrayList<>();
                final String [] sourceToDestination = line.split(" ")[0].split("-");
                source = sourceToDestination[0];
                destination = sourceToDestination[2];
                notFirstMap = true;
            } else {
                final List<Long> numbers = Stream.of(line.split(" ")).map(Long::parseLong).toList();

                mappings.add(new AlmanacMapping(numbers.get(0), numbers.get(1), numbers.get(2)));
            }
        }

        maps.put(source, new AlmanacMap(source, destination, mappings));
        return maps;
    }

    private static List<Long> getSeedsFromFirstInputLine(String line) {
        return Arrays.stream(line.split(":")[1].trim().split("\\s+"))
                .map(Long::parseLong)
                .toList();
    }

    private static List<String> extractInputLines() throws IOException {
        try (InputStream resource = Main.class.getResourceAsStream(INPUT_FILENAME)) {
            if (resource == null) {
                throw new RuntimeException(WRONG_INPUT_FILE_MESSAGE);
            }

            return new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines()
                    .toList();
        }
    }

    public static class AlmanacMap {
        String source;
        String destination;
        List<AlmanacMapping> mappings;

        public AlmanacMap(final String source, final String destination, List<AlmanacMapping> mappings) {
            this.source = source;
            this.destination = destination;
            this.mappings = mappings;
        }

        public String getDestination() {
            return destination;
        }

        public long getDestinationNumber(long sourceNumber) {
            for(AlmanacMapping mapping : mappings) {
                boolean isSourceNumberSmallerThanMaxRange = sourceNumber < mapping.sourceRange() + mapping.sourceLength();
                boolean isSourceNumberBiggerThanMinRange = sourceNumber >= mapping.sourceRange();

                if (isSourceNumberBiggerThanMinRange && isSourceNumberSmallerThanMaxRange) {
                    return mapping.destinationRange() + (sourceNumber - mapping.sourceRange());
                }
            }

            return sourceNumber;
        }
    }

    public record AlmanacMapping(long destinationRange, long sourceRange, long sourceLength) {}
}
