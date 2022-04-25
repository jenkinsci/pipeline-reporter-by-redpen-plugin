package org.jenkinsci.plugins.redpen.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private FileUtils() {}

    public static String getNewFile(String path, String result, String epochTime) {
        return String.format("%s_%s_%s", path, result, epochTime);
    }

    public static List<File> listFilesForFolder(File folder) {
        List<File> files = new ArrayList<>();
        File[] directory = folder.listFiles();
        if (folder.exists() && directory != null) {

            for (File fileEntry : directory) {
                if (fileEntry.isDirectory()) {
                    files.addAll(FileUtils.listFilesForFolder(fileEntry));
                } else {
                    files.add(fileEntry);
                }
            }
        }
        if (folder.exists() && !folder.isDirectory()) {
            files.add(folder);
        }

        return files;
    }
}
