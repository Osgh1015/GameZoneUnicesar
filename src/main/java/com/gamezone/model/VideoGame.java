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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    @Override
    public String getDescription() {
        return String.format(
                "Video Game: %s | Platform: %s | Genre: %s | Age Rating: %s | Price: %.2f | Stock: %d",
                getTitle(), platform, genre, ageRating, getPrice(), getQuantity());
    }
}