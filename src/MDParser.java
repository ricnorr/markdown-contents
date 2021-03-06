import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class MDParser {
    public static void main(String[] args) {
        try {
            MDParser parser = new MDParser();
            Path path = parser.validateArgs(args);
            parser.printContents(path);
            System.out.println();
            parser.writeFileInOutput(path);
        } catch (ParserException e) {
            System.err.println("Exception while generating table of contents: " + e.getMessage());
        }
    }

    private String prepareTitle(String title) {
        return "(#" + title.toLowerCase().replaceAll("\\s+", "-") + ")";
    }


    private void writeFileInOutput(Path filePath) throws ParserException {
        try (BufferedReader bufferedReader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new ParserException("IO problem while reading " + filePath, e);
        }
    }

    private Path validateArgs(String[] args) throws ParserException {
        if (args == null || args.length != 1 || args[0] == null) {
            throw new ParserException("Illegal arguments format. Should be <filename>");
        }
        try {
            return Path.of(args[0]);
        } catch (InvalidPathException e) {
            throw new ParserException("File-name <" + args[0] + "> is not a correct path");
        }
    }

    private int getLevelForLine(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == '#') {
            i++;
        }
        int level = i;
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        return i < line.length() && level != i ? level : 0;
    }

    private void printContents(Path path) throws ParserException {
        Deque<Integer> levelStack = new ArrayDeque<>();
        Deque<Integer> titlesOnLevelStack = new ArrayDeque<>();
        int previousLevel = 0;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int currentLevel = getLevelForLine(line);
                if (currentLevel == 0) {
                    continue;
                }
                if (currentLevel > previousLevel) {
                    levelStack.add(currentLevel);
                    titlesOnLevelStack.add(1);
                }
                if (currentLevel == previousLevel) {
                    titlesOnLevelStack.add(titlesOnLevelStack.removeLast() + 1);
                }
                if (currentLevel < previousLevel) {
                    while (!levelStack.isEmpty() && levelStack.peekLast() > currentLevel) {
                        titlesOnLevelStack.removeLast();
                        levelStack.removeLast();
                    }
                    if (levelStack.isEmpty() || levelStack.peekLast() != currentLevel) {
                        levelStack.add(currentLevel);
                        titlesOnLevelStack.add(1);
                    } else {
                        titlesOnLevelStack.add(titlesOnLevelStack.removeLast() + 1);
                    }
                }
                String title = line.substring(currentLevel + 1);
                System.out.println("\t".repeat(currentLevel - 1) +
                        titlesOnLevelStack.peekLast() + ". " + "[" + title + "]" + prepareTitle(title));
                previousLevel = currentLevel;
            }
        } catch (IOException e) {
            throw new ParserException("IO problem happened while reading \"" + e.getMessage() + "\"", e);
        }
    }
}
