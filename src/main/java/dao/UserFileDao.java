package dao;

import org.apache.ibatis.annotations.Param;
import pojo.UserFile;

import java.util.List;

public interface UserFileDao {
    void addUserFile(UserFile userfile);

    UserFile findUserFileById(Integer id);

    List<UserFile> findUserFiles(UserFile userFile);

    List<UserFile> wildcardFindUserFiles(UserFile userFile);

    void updateUserFileById(@Param("id") Integer id, @Param("userFile") UserFile userFile);

    void deleteUserFileById(Integer id);

    void deleteUserFileByIds(List<Integer> userFileIds);
}
