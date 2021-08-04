package org.jenkinsci.plugins.redpen.models;

public class CommentRequestModel {
    private String comment;

    public CommentRequestModel(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
