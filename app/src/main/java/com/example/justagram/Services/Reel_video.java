package com.example.justagram.Services;

import java.io.Serializable;

public class Reel_video implements Serializable {
    private String id;
    private String videoUrl;
    private String thumbnailUrl;
    private String title;
    private String description;
    private String date;
    private int likes;
    private int comments;
    private int views;
    private String permalink;
    private String caption;
    private String timestamp;
    private String mediaType;

    public Reel_video() {
    }

    public Reel_video(String id, String videoUrl, String thumbnailUrl, String title,
                      String description, String date, int likes, int comments, int views) {
        this.id = id;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.description = description;
        this.date = date;
        this.likes = likes;
        this.comments = comments;
        this.views = views;
    }

    // Getters
    public String getId() {
        return id;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title != null ? title : "Video Title Here";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "Description here...";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date != null ? date : "Aug 8, 2023";
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    // Utility methods
    public String getFormattedViews() {
        if (views >= 1000000) {
            return String.format("%.1fM", views / 1000000.0);
        } else if (views >= 1000) {
            return String.format("%.1fK", views / 1000.0);
        }
        return String.valueOf(views);
    }

    public String getFormattedLikes() {
        if (likes >= 1000000) {
            return String.format("%.1fM", likes / 1000000.0);
        } else if (likes >= 1000) {
            return String.format("%.1fK", likes / 1000.0);
        }
        return String.valueOf(likes);
    }

    public String getFormattedComments() {
        if (comments >= 1000) {
            return String.format("%.1fK", comments / 1000.0);
        }
        return String.valueOf(comments);
    }
}