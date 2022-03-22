package org.jenkinsci.plugins.redpen.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestFrameWork {
    private String value;
    private String displayName;
    private String path;
}
