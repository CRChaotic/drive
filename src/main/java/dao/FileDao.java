package dao;

import org.apache.ibatis.annotations.Param;
import pojo.File;

import java.util.List;

public interface FileDao {
    void addFile(File file);
    List<File> findFiles(File file);
    File findFileById(String id);
    void updateFileById(@Param("id") String id, @Param("file") File file);
    void deleteFileById(String id);
}
