package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpXmlLogStackerApp {
    private static final int EXECUTION_DELAY_MS = 5000;
    public static String targetDirectory = System.getProperty("user.dir") + "/output/";
    public static String stackedDirectory = targetDirectory + "stacked/";

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Files will be taken from " + targetDirectory + " and stacked in " + stackedDirectory);

        while (true) {

            Set<String> fileNames = getFileList(targetDirectory);

            if (fileNames.size() > 0) {
                for (String fileName : fileNames) {
                    Set<String> stackedFileNames =
                            getFileList(targetDirectory).stream()
                                    .filter(name -> name.substring(0, name.indexOf('-'))
                                            .equals(fileName.substring(0, fileName.indexOf('-'))))
                                    .collect(Collectors.toSet());
                    int firstFileNumber = stackedFileNames.size();
                    if (firstFileNumber == 0) firstFileNumber = 1;
                    addStackedFiles(fileName, firstFileNumber);
                }
            }
            Thread.sleep(EXECUTION_DELAY_MS);
        }
    }

    private static Set<String> getFileList(String dir) {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage() + " while reading directory content!");
            e.printStackTrace();
            return null;
        }
    }

    private static void addStackedFiles(String fileName, int firstFileNumber) {
        int count = firstFileNumber;
        List<String> currentStack = new ArrayList<>();

        // reading all lines
        Path logFile = Paths.get(targetDirectory + fileName);
        List<String> logFileContent = new ArrayList<>();
        try {
            logFileContent.addAll(Files.readAllLines(logFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Exception " + e.getMessage() + " while reading file" + fileName + "!");
            e.printStackTrace();
        }

        // writing stacks of 100 lines each
        for (int line = 1 + (100 * firstFileNumber - 1); line < logFileContent.size(); line++) {
            currentStack.add(logFileContent.get(line));
            if (line % 100 == 0) {
                writeStack(fileName, count, currentStack);
                currentStack.clear();
                count++;
            }
        }
    }

    private static void writeStack(String fileName, int count, List<String> currentStack) {
        // locating/creating file and directory
        Path targetFile = Paths.get(stackedDirectory + fileName.substring(0, fileName.indexOf('.'))
                + String.format("%04d", count) + ".log");
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
    }
}