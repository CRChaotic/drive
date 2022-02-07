package dao;

import org.apache.ibatis.annotations.Param;
import pojo.ShareInfo;

import java.util.List;

public interface ShareInfoDao {
    void addShareInfo(ShareInfo shareInfo);
    void addUserFilesToShareInfoById(@Param("id") String id, @Param("userFileIds") List<Integer> userFileIds);
    void deleteUserFilesFromShareInfoById(@Param("id") String id, @Param("userFileIds") List<Integer> userFileIds);
    List<ShareInfo> findShareInfo(ShareInfo shareInfo);
    ShareInfo findShareInfoById(String id);
    void updateShareInfoById(@Param("id") String id, @Param("shareInfo") ShareInfo shareInfo);
    void deleteShareInfoById(String id);
}
