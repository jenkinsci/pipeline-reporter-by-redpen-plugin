package org.jenkinsci.plugins.redpen.models;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ParameterModel {

    private String secret;
    private String issueKey;
    private String logAbsolutePath;
    private String displayName;
    private String result;
    private String projectName;
    private Instant buildTriggerTime;
    private String buildNumber;
    private String serviceConnectionId;
    private String e2eTestFrameWork;
    private String unitTestFrameWork;
    private String logFileLocation;
    private String coverageFrameWork;
}
