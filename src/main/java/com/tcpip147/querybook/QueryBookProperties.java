package com.tcpip147.querybook;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "query-book")
public class QueryBookProperties {

    private String root;

    private boolean devMode;

    private boolean injectComment;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public boolean isInjectComment() {
        return injectComment;
    }

    public void setInjectComment(boolean injectComment) {
        this.injectComment = injectComment;
    }
}
