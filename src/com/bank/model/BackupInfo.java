package com.bank.model;

import java.io.Serializable;
import java.util.Date;

/** Сведения о файле резервной копии для отображения в списке. */
public class BackupInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    private Date createdAt;
    private long sizeBytes;

    public BackupInfo() {
    }

    public BackupInfo(String fileName, Date createdAt, long sizeBytes) {
        this.fileName = fileName;
        this.createdAt = createdAt;
        this.sizeBytes = sizeBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public long getSizeKb() {
        return Math.max(1, sizeBytes / 1024);
    }
}
