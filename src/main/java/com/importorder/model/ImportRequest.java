package com.importorder.model;

import com.importorder.model.enums.ImportRequestStatus;

import java.util.Objects;

/**
 * Sales department import request header.
 */
public class ImportRequest {

    private Long id;
    private String requestCode;
    private String requestDate;
    private ImportRequestStatus status;
    private String createdAt;

    public ImportRequest() {
    }

    public ImportRequest(Long id, String requestCode, String requestDate,
                         ImportRequestStatus status, String createdAt) {
        this.id = id;
        this.requestCode = requestCode;
        this.requestDate = requestDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public ImportRequest(String requestCode, String requestDate, ImportRequestStatus status) {
        this(null, requestCode, requestDate, status, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public ImportRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ImportRequestStatus status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return requestCode != null ? requestCode : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportRequest that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
