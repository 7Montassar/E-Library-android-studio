package com.example.e_library.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Book {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String category;
    private byte[] pdfFile;
    private String coverUrl; // This will store image_url
    private String createdAt;

    // Exclude pdf_file from the basic model

    public Book(Long id, String title, String author, String description, String category, String coverUrl, String createdAt, byte[] pdfFile) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.category = category;
        this.pdfFile = pdfFile;
        this.coverUrl = coverUrl;
        this.createdAt = createdAt;
    }
    public Book(Long id, String title, String author, String category, String coverUrl) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.coverUrl = coverUrl;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getCoverUrl() { return coverUrl; }
    public String getCreatedAt() { return createdAt; }


    protected Book(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        title = in.readString();
        author = in.readString();
        category = in.readString();
        coverUrl = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(category);
        dest.writeString(coverUrl);
    }

    public int describeContents() {
        return 0;
    }


    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}