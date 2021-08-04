package org.jenkinsci.plugins.redpen.util;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IssueKeyExtractor {

    private IssueKeyExtractor() {
        // empty
    }

    private static final String SEPARATOR = "[\\s\\p{Punct}]";
    // zero-width positive lookbehind
    private static final String KEY_PREFIX_REGEX = "(?:(?<=" + SEPARATOR + ")|^)";
    // max of 256 chars in Issue Key project name and 100 for the issue number
    private static final String KEY_BODY_REGEX =
            "(\\p{Lu}[\\p{Lu}\\p{Digit}_]{1,255}-\\p{Digit}{1,100})";
    // zero-width positive lookahead
    private static final String KEY_POSTFIX_REGEX = "(?:(?=" + SEPARATOR + ")|$)";

    private static final String ISSUE_KEY_REGEX =
            KEY_PREFIX_REGEX + KEY_BODY_REGEX + KEY_POSTFIX_REGEX;
    private static final Pattern PROJECT_KEY_PATTERN = Pattern.compile(ISSUE_KEY_REGEX);

    public static String extractIssueKey(final String text) {
        String issueKey = "";

        if (StringUtils.isBlank(text)) {
            return issueKey;
        }

        final Matcher match = PROJECT_KEY_PATTERN.matcher(text);
        if (match.find()) {
            issueKey = match.group(1)
                    .trim();
        }

        return issueKey;
    }
}
