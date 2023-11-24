package org.example;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GroupingProcessor {
    public static void main(String[] args) {
        String filePath = args[0]; // Имя файла для сохранения результатов

        try {
            // Скачивание и обработка .gz файла
            String gzFilePath = downloadFile("https://github.com/PeacockTeam/new-job/releases/download/v1.0/lng-4.txt.gz");
            List<String> lines = readLinesFromGzipFile(gzFilePath);

            long startTime = System.currentTimeMillis();
            List<List<String>> groups = findGroups(lines);
            writeGroupsToFile(groups, filePath);
            long endTime = System.currentTimeMillis();

            System.out.println("Number of groups with more than one element: " + groups.size());
            System.out.println("Execution time: " + (endTime - startTime) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String downloadFile(String url) throws IOException {
        try (InputStream in = new URL(url).openStream();
             BufferedInputStream bis = new BufferedInputStream(in);
             FileOutputStream fos = new FileOutputStream("downloaded.gz");
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        }

        return "downloaded.gz";
    }

    private static List<String> readLinesFromGzipFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();

        try (InputStream is = Files.newInputStream(Path.of(filePath));
             InputStream gzippedInputStream = new GzipCompressorInputStream(is);
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzippedInputStream, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }

    private static List<List<String>> findGroups(List<String> lines) {
        List<List<String>> groups = new ArrayList<>();
        Map<String, List<String>> valueToLinesMap = new HashMap<>();

        for (String line : lines) {
            String[] values = line.split(";");
            boolean foundGroup = false;

            for (String value : values) {
                if (!value.isEmpty()) {
                    List<String> group = valueToLinesMap.get(value);
                    if (group != null) {
                        group.add(line);
                        foundGroup = true;
                        break;
                    }
                }
            }

            if (!foundGroup) {
                List<String> newGroup = new ArrayList<>();
                newGroup.add(line);
                for (String value : values) {
                    if (!value.isEmpty()) {
                        valueToLinesMap.put(value, newGroup);
                    }
                }
            }
        }

        Set<List<String>> uniqueGroups = new HashSet<>(valueToLinesMap.values());

        for (List<String> group : uniqueGroups) {
            if (group.size() > 1) {
                groups.add(group);
            }
        }

        groups.sort(Comparator.comparingInt(List<String>::size).reversed()); // Сортировка по размеру, от большего к меньшему
        return groups;
    }



    private static void writeGroupsToFile(List<List<String>> groups, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (int i = 0; i < groups.size(); i++) {
                List<String> group = groups.get(i);
                writer.println("Group " + (i + 1));
                for (String line : group) {
                    writer.println(line);
                }
                writer.println();
            }
        }
    }
}