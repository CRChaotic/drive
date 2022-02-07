package pojo;

public class File {
    private String id;
    private Long size;
    private FileStatus status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "File{" +
                "id='" + id + '\'' +
                ", size=" + size +
                ", status=" + status.name() +
                '}';
    }
}
