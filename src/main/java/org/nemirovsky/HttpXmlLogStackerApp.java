package org.nemirovsky;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpXmlLogStackerApp {

    // Delay between cycles of analyzing log files
    private static final int EXECUTION_DELAY_MS = 5000;

    public static String sourceDirectory = System.getProperty("user.home") + File.separator + "output";
    public static String targetDirectory = sourceDirectory + File.separator + "stacked";

    public static void main(String[] args) throws InterruptedException, IOException {

        System.out.println("Files will be taken from " + sourceDirectory + " and put in " + targetDirectory);

        while (true) {

            Set<String> sourceFileNames;

            if (Files.exists(Path.of(sourceDirectory))) {
                sourceFileNames = new HashSet<>(getFileList(sourceDirectory));
            } else {
                System.out.println("Cannot find source directory!");
                break;
            }

            // TO REMOVE
            System.out.println("SourceFileNames = " + sourceFileNames);

            if (sourceFileNames.size() > 0) {
                for (String fileName : sourceFileNames) {

                    // TO REMOVE
                    System.out.println("Doing file " + fileName);

                    Set<String> stackedFileNames = new HashSet<>();

                    if (Files.exists(Path.of(targetDirectory))) {
                        stackedFileNames = getFileList(targetDirectory).stream()
                                .filter(name -> name.substring(0, name.indexOf('-'))
                                        .equals(fileName.substring(0, fileName.indexOf('-')))).collect(Collectors.toSet());
                    }

                    // TO REMOVE
                    System.out.println("stackedFileNames = " + stackedFileNames);

                    int firstFileNumber = stackedFileNames.size();

                    // Check if the last file has precisely 100 entries (then do next)
                    List<String> lastFileContent = new ArrayList<>();
                    if (firstFileNumber > 0) {
                        Path lastFile = Paths.get(targetDirectory + File.separator + fileName.substring(0, fileName.indexOf('.'))
                                + "-" + String.format("%04d", firstFileNumber) + ".log");
                        lastFileContent = Files.readAllLines(lastFile, StandardCharsets.UTF_8);
                    }
                    if (firstFileNumber >= 1 && lastFileContent.size() == 100 || firstFileNumber == 0) {
                        firstFileNumber++;
                    }
                    addStackedFiles(fileName, firstFileNumber);
                }
            } else {
                System.out.println("There are no files in source directory!");
                break;
            }
            Thread.sleep(EXECUTION_DELAY_MS);
        }
    }

    private static Set<String> getFileList(String dir) {

        // TO DELETE
        System.out.println("dir = " + dir);

        try (Stream<Path> stream = Files.list(Path.of(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage() + " while reading directory content!");
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    private static void addStackedFiles(String fileName, int firstFileNumber) {

        // TO DELETE
        System.out.println("Adding stack: fileName = " + fileName + ", firstFileNumber = " + firstFileNumber);

        int count = firstFileNumber;
        List<String> currentStack = new ArrayList<>();

        // reading all lines
        Path logFile = Paths.get(sourceDirectory + File.separator + fileName);
        List<String> logFileContent = new ArrayList<>();
        try {
            logFileContent.addAll(Files.readAllLines(logFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Exception " + e.getMessage() + " while reading file " + fileName + "!");
            e.printStackTrace();
        }

        // TO DELETE
        System.out.println("Read lines size: " + logFileContent.size());

        // writing stack files of 100 lines each
        for (int line = 1 + (100 * (firstFileNumber - 1)); line < logFileContent.size(); line++) {

            // To DELETE
            System.out.println("line = " + line + ", content(line) = " + logFileContent.get(line));

            currentStack.add(logFileContent.get(line));
            if (line % 100 == 0) {
                writeStack(fileName, count, currentStack);
                currentStack.clear();
                count++;
            }
        }
        writeStack(fileName, count, currentStack);
    }

    private static void writeStack(String fileName, int count, List<String> currentStack) {

        // TO DELETE
        System.out.println("Writing file: " + targetDirectory + File.separator + fileName.substring(0, fileName.indexOf('.'))
                + "-" + String.format("%04d", count) + ".log, currentStack.size = " + currentStack.size());

        // locating/creating file and directory
        Path targetFile = Paths.get(targetDirectory + File.separator + fileName.substring(0, fileName.indexOf('.'))
                + "-" + String.format("%04d", count) + ".log");
        try {
            Files.createDirectories(targetFile.getParent()); // create if not exists yet
            if (!Files.exists(targetFile))
                Files.createFile(targetFile); // create if not exists yet
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage() + " while creating file/directory!");
            e.printStackTrace();
        }

        // writing current stack of lines
        try {
            Files.write(targetFile, currentStack, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Exception " + e.getMessage() + " while writing to file!");
            e.printStackTrace();
        }
        System.out.println("Files written!");
    }
}