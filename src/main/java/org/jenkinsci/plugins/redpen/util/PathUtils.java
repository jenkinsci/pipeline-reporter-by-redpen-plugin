package org.jenkinsci.plugins.redpen.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
    private PathUtils() {
    }

    public static String getPath(String path) {
        Path relativePath = Paths.get(path);
        return relativePath.toString();
    }
}
