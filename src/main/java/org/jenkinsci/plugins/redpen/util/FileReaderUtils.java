package org.jenkinsci.plugins.redpen.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Logger;

public class FileReaderUtils {
    private static final Logger LOGGER = Logger.getLogger(FileReaderUtils.class.getName());

    private FileReaderUtils() {}

    public static String readFile(String filePath, String title) {
        String comment = "";
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

        try {
            if (filePath.endsWith(".json")) {
                JSONObject a = (JSONObject) parser.parse(new FileReader(filePath));


                Object numFailedTests = a.get("numFailedTests");
                Object numPassedTests = a.get("numPassedTests");

                comment = "{panel:title="+ title +"}" +
                        "{color:red}*FAILED*{color}: "+ numFailedTests +" {color:green}*PASSED*{color}: "+ numPassedTests +
                        "{panel}";
            }
        } catch (FileNotFoundException | ParseException | ClassCastException e) {
            LOGGER.warning("File not found to read : " + filePath);
        }

        return comment;
    }
}
