package ru.choomandco.VLCacheFinal.fileGarbage.orm.psqlCacheStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jdk.jfr.Label;

import java.sql.Timestamp;

@Entity
@Table(name = "cache_status")
public class CacheStatus {

    @Id
    private String url;
    @Label("is_cached")
    private boolean isCached;
    @Label("cached_at")
    private Timestamp cachedAt;

    public CacheStatus() {}

    public CacheStatus(String url, boolean isCached, Timestamp cachedAt) {
        this.url = url;
        this.isCached = isCached;
        this.cachedAt = cachedAt;
    }

    public CacheStatus(String url, boolean isCached) {
        this.url = url;
        this.isCached = isCached;
    }

    public String getUrl() {
        return url;
    }

    public boolean isCached() {
        return isCached;
    }

    public Timestamp getCachedAt() {
        return cachedAt;
    }

    @Override
    public String toString() {
        return "CacheStatus{" +
                "url=" + url +
                ", isCached=" + isCached +
                ", cachedAt=" + cachedAt +
                '}';
    }
}
