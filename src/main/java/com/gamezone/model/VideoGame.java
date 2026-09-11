package com.gamezone.model;

/**
 * Represents a video game sold by GameZone Unicesar.
 */
public class VideoGame extends Product {

    private String platform;
    private String genre;
    private String ageRating;

    public VideoGame(String id, String title, double price, int quantity,
                     String platform, String genre, String ageRating) {
        super(id, title, price, quantity);
        this.platform = platform;
        this.genre = genre;
        this.ageRating = ageRating;
    }

    @Override
    public String getDescription() {
        return null;
    }
}