package org.jenkinsci.plugins.redpen.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.jenkinsci.plugins.redpen.constant.Constants;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

public class FileReaderUtils {
    private static final Logger LOGGER = Logger.getLogger(FileReaderUtils.class.getName());

    private FileReaderUtils() {
    }

    public static String readFile(String filePath, String basePath, String frameWork, String title) {
        String comment = "";
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            switch (title) {
                case Constants.UNIT_TEST: {
                    if (Constants.JEST.equals(frameWork) && filePath.endsWith(".json")) {

                        JSONObject result = objectMapper.readValue(new File(filePath), JSONObject.class);

                        Object numFailedTests = result.get("numFailedTests");
                        Object numPassedTests = result.get("numPassedTests");

                        comment = "{panel:title=" + title + "}" +
                                "{color:green}*PASSED*{color}: *" + numPassedTests + "* {color:red}*FAILED*{color}: *" + numFailedTests +
                                "*{panel}";
                    }
                }
                break;
                case Constants.E2E_TEST: {
                    filePath = filePath.contains("E2E-report.pdf") ? PathUtils.getPath(String.format("%s%s", basePath, Constants.WEB_DRIVER_IO_PATH_JSON)) : filePath;
                    if (Constants.WEB_DRIVER_IO.equals(frameWork) && filePath.endsWith(".json")) {
                        JSONObject result = objectMapper.readValue(new File(filePath), JSONObject.class);

                        LinkedHashMap<String,Long> metrics = (LinkedHashMap<String, Long>) result.get("metrics");

                        Object passed = metrics.get("passed");
                        Object failed = metrics.get("failed");
                        Object skipped = metrics.get("skipped");

                        comment = "{panel:title=" + title + "}" +
                                "{color:green}*PASSED*{color}: *" + passed + "* {color:orange}*SKIPPED*{color}: *" + skipped + "* {color:red}*FAILED*{color}: *" + failed +
                                "*{panel}";
                    }
                }
                break;
                default:
                    break;
            }


        } catch (IOException | ClassCastException e) {
            LOGGER.warning("File not found to read : " + filePath);
        }

        return comment;
    }
}
