package com.crawler.lucene;

public class Tweet {
    private String id;
    private String creationDate;
    private int likeCount;
    private int relpyCount;
    private String text;

    public Tweet(String creationDate, String id, int likeCount, String text, int relpy_count) {
        this.creationDate = creationDate;
        this.id = id;
        this.likeCount = likeCount;
        this.text = text;
        this.relpyCount = relpy_count;
    }
    
    public String getLikeCount() {
        return Integer.toString(this.likeCount);
    }
    public String getRelyCount() {
        return Integer.toString(this.relpyCount);
    }

    public String getData() {
        return this.creationDate;
    }

    public String getId(){
        return this.id;
    }

    public String getText(){
        return text;
    }
}