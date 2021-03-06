package service;

import pojo.ShareInfo;
import pojo.User;
import pojo.UserFile;

import java.sql.Timestamp;
import java.util.List;

public interface ShareFileService {
    void saveShareInfo(User user, ShareInfo shareInfo);
    List<ShareInfo> getAllShareInfo(User user);
    ShareInfo getShareInfoByIdAndAccessToken(String shareId, String accessToken, int directoryId);
    UserFile getSharedUserFileByUserFileId(String shareId, String accessToken, int userFileId);
    void modifyShareInfoAccessTokenById(User user,String shareId,String accessToken);
    void modifyShareInfoExpiryTimeById(User user, String shareId, Timestamp expiryTime);
    void removeShareInfoById(User user,String shareId);
}
