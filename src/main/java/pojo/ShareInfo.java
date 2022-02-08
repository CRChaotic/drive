package pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.List;

public class ShareInfo {
    private String id;
    private String username;
    private String accessToken;
    private Timestamp expiryTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Timestamp createdTime;
    private Integer rootDirectory;
    private List<UserFile> userFiles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Timestamp getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Timestamp expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Integer getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(Integer rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public List<UserFile> getUserFiles() {
        return userFiles;
    }

    public void setUserFiles(List<UserFile> userFiles) {
        this.userFiles = userFiles;
    }

    @Override
    public String toString() {
        return "ShareInfo{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", expiryTime=" + expiryTime +
                ", createdTime=" + createdTime +
                ", rootDirectory=" + rootDirectory +
                ", userFiles=" + userFiles +
                '}';
    }
}
