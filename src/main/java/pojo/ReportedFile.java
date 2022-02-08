package pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public class ReportedFile {
    private Integer id;
    private String username;
    private String fileId;
    private String reason;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp createdTime;
    private FileStatus fileStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    @Override
    public String toString() {
        return "ReportedFile{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fileId='" + fileId + '\'' +
                ", reason='" + reason + '\'' +
                ", createdTime=" + createdTime +
                ", fileStatus=" + fileStatus.name() +
                '}';
    }
}
