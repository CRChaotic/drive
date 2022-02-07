package pojo;

import java.sql.Timestamp;

public class UserFile {
    private Integer id;
    private String username;
    private Integer directory;
    private String filename;
    private Long size;
    private String type;
    private Timestamp createdTime;
    private UserFileStatus userFileStatus;
    private FileStatus fileStatus;
    private String fileId;

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

    public Integer getDirectory() {
        return directory;
    }

    public void setDirectory(Integer directory) {
        this.directory = directory;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public UserFileStatus getUserFileStatus() {
        return userFileStatus;
    }

    public void setUserFileStatus(UserFileStatus userFileStatus) {
        this.userFileStatus = userFileStatus;
    }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    @Override
    public String toString() {
        return "UserFile{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", directory=" + directory +
                ", fileId='" + fileId + '\'' +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                ", type='" + type + '\'' +
                ", createdTime=" + createdTime +
                ", userFileStatus=" + userFileStatus.name() +
                ", fileStatus=" + fileStatus.name() +
                '}';
    }
}
