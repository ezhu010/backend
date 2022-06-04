package com.crawler.lucene;

public class Tweet {
    private String id;
    private String creationDate;
    private int likeCount;
    private int follower_count;
    private String text;

    public Tweet(String creationDate, String id, int likeCount, String text, int follower_count) {
        this.creationDate = creationDate;
        this.id = id;
        this.likeCount = likeCount;
        this.text = text;
        this.follower_count = follower_count;
    }
    
    public String getLikeCount() {
        return Integer.toString(this.likeCount);
    }
    public String getFollowerCount() {
        return Integer.toString(this.follower_count);
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