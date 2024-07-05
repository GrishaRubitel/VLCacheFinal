package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlVideos;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jdk.jfr.Label;

import java.sql.Timestamp;

@Entity
@Table(name = "videos")
public class Videos {
    @Id
    private String url;
    @Label("user_id")
    private String title;
    @Label("file_path")
    private String filePath;
    @Label("uploaded_at")
    private Timestamp uploadedAt;

    public Videos() {}

    public Videos(String title, String url, String filePath, Timestamp uploadedAt) {
        this.title = title;
        this.url = url;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
    }

    public Videos(String url, int userId, String title, String filePath, Timestamp uploadedAt) {
        this.url = url;
        this.title = title;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFilePathWithoutVideos() {
        return filePath.trim().replaceAll("/videos", "");
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "Video{" +
                "url=" + url +
                ", title='" + title + '\'' +
                ", filePath='" + filePath + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}
