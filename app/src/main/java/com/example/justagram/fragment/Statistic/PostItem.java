package com.example.justagram.fragment.Statistic;

public class PostItem {

    private String PostID;
    private String mediaUrl;       // đường dẫn ảnh hoặc video
    private String mediaType;      // "image" hoặc "video"
    private String thumbnailUrl;   // 🆕 URL ảnh thumbnail (nếu là video)
    private int likeCount;         // số lượng like
    private int commentCount;      // số lượng comment
    private boolean isSelected = false; // chọn nhiều bài

    // --- Constructor chuẩn hóa ---
    public PostItem(String PostID, String mediaUrl, String mediaType, int likeCount, int commentCount) {
        this.PostID = PostID; // ⚡ thêm dòng này
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    // --- Getter ---
    public String getPostId() {
        return PostID;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public boolean isSelected() {
        return isSelected;
    }

    // --- Setter ---
    public void setPostID(String PostID) {
        this.PostID = PostID;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}