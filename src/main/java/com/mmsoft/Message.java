package com.mmsoft;

/**
 * Author: Van Phan <vanthuyphan@gmail.com> -- 6/19/16.
 */
public class Message {
    private String content;
    private String title;

    public Message(String content, String title) {
        this.content = content;
        this.title = title;
    }

    public Message() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
