package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlUploadLogs;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jdk.jfr.Label;

import java.sql.Timestamp;

@Entity
@Table(name = "upload_logs")
public class UploadLogs {

    @Id
    private String url;
    private String status;
    @Label("created_at")
    private Timestamp createdAt;

    public UploadLogs() {}

    public UploadLogs(String url, String status, Timestamp createdAt) {
        this.url = url;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UploadLogs(String url, String status) {
        this.url = url;
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "UploadLogs{" +
                "url=" + url +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
