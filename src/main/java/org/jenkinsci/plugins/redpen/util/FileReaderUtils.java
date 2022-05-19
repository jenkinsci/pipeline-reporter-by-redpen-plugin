package org.jenkinsci.plugins.redpen.util;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.bouncycastle.util.encoders.UTF8;
import org.jenkinsci.plugins.redpen.constant.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class FileReaderUtils {
    private static final Logger LOGGER = Logger.getLogger(FileReaderUtils.class.getName());

    private FileReaderUtils() {
    }

    public static String readFile(String filePath, String basePath, String frameWork, String title) {
        String comment = "";
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

        try {

            switch (title) {
                case Constants.UNIT_TEST: {
                    if (Constants.JEST.equals(frameWork) && filePath.endsWith(".json")) {
                        JSONObject a = (JSONObject) parser.parse(new FileReader(filePath));
                        Object numFailedTests = a.get("numFailedTests");
                        Object numPassedTests = a.get("numPassedTests");

                        comment = "{panel:title=" + title + "}" +
                                "{color:green}*PASSED*{color}: " + numPassedTests + " {color:red}*FAILED*{color}: " + numFailedTests +
                                "{panel}";
                    }
                }
                break;
                case Constants.E2E_TEST: {
                    filePath = filePath.contains(Constants.WEB_DRIVER_IO_PATH) ? PathUtils.getPath(String.format("%s%s", basePath, "/reports/html-reports/E2E-report.json")) : filePath;
                    if (Constants.WEB_DRIVER_IO.equals(frameWork) && filePath.endsWith(".json")) {
                        JSONObject a = (JSONObject) parser.parse(new FileReader(filePath));
                        JSONObject metrics = (JSONObject)  a.get("metrics");

                        Object passed = metrics.get("passed");
                        Object failed = metrics.get("failed");
                        Object skipped = metrics.get("skipped");

                        comment = "{panel:title=" + title + "}" +
                                "{color:green}*PASSED*{color}: " + passed + "{color:red}*SKIPPED*{color}: " + skipped + "{color:red}*FAILED*{color}: " + failed +
                                "{panel}";
                    }
                }
                break;
                default:
                    break;
            }


        } catch (FileNotFoundException | ParseException | ClassCastException e) {
            LOGGER.warning("File not found to read : " + filePath);
        }

        return comment;
    }
}
