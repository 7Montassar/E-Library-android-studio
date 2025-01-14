package com.example.e_library.models;

// models/DownloadedBook.java
public class DownloadedBook {

    // Constructor
    public DownloadedBook(long id, String userId, long bookId, String downloadDate,
                          String lastReadDate, int lastPageRead, int totalPages,
                          float readingProgress, String title, String author, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.downloadDate = downloadDate;
        this.lastReadDate = lastReadDate;
        this.lastPageRead = lastPageRead;
        this.totalPages = totalPages;
        this.readingProgress = readingProgress;
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
    }


    public int getId() {
        return (int) id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public String getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(String downloadDate) {
        this.downloadDate = downloadDate;
    }

    public String getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(String lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    public int getLastPageRead() {
        return lastPageRead;
    }

    public void setLastPageRead(int lastPageRead) {
        this.lastPageRead = lastPageRead;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public float getReadingProgress() {
        return readingProgress;
    }

    public void setReadingProgress(float readingProgress) {
        this.readingProgress = readingProgress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private long id;
    private String userId;
    private long bookId;
    private String downloadDate;
    private String lastReadDate;
    private int lastPageRead;
    private int totalPages;
    private float readingProgress;
    private String title;
    private String author;
    private String imageUrl;

}