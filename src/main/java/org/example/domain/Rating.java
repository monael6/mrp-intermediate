package org.example.domain;

public class Rating {
    public int id;
    public int mediaId;
    public int userId;
    public int stars;
    public String comment;          // PUBLIC comment (null wenn nicht confirmed)
    public boolean commentConfirmed;
    public int likes;
    public String createdAt;
}
