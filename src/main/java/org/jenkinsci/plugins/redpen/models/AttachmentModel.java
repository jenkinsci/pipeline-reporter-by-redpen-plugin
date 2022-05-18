package org.jenkinsci.plugins.redpen.models;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentModel {
    private String comment;
    private List<String> attachments;
}
