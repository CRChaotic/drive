package service.implement;

import dao.ShareInfoDao;
import dao.UserFileDao;
import exception.*;
import org.springframework.stereotype.Service;
import pojo.*;
import service.ShareFileService;
import service.UserService;
import utils.FileTypeConverter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service("shareFileService")
public class ShareFileServiceImpl implements ShareFileService {
    private final ShareInfoDao shareInfoDao;
    private final UserFileDao userFileDao;
    private final UserService userService;

    public ShareFileServiceImpl(ShareInfoDao shareInfoDao, UserFileDao userFileDao, UserService userService) {
        this.shareInfoDao = shareInfoDao;
        this.userFileDao = userFileDao;
        this.userService = userService;
    }

    @Override
    public void saveShareInfo(User user, ShareInfo shareInfo) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        List<Integer> userFileIds = new ArrayList<>();
        //check out every single user file whether it belongs to the user
        shareInfo.getUserFiles().forEach(userFile -> {
            UserFile uf = userFileDao.findUserFileById(userFile.getId());
            if (!uf.getUsername().equals(user.getUsername())) {
                throw new UserFileOwnerException();
            }
            if (uf.getUserFileStatus() == UserFileStatus.NORMAL && uf.getFileStatus() == FileStatus.NORMAL) {
//                userFileIds.add(uf.getId());
                if (uf.getType().equals(FileTypeConverter.DIRECTORY_TYPE))
                    recursivelySaveSharedUserFiles(uf, userFileIds);
                else
                    userFileIds.add(uf.getId());
            }
        });
        shareInfo.setId(UUID.randomUUID().toString());
        shareInfo.setUsername(user.getUsername());
        //choose the smallest directory id as root directory
        UserFile uf = shareInfo.getUserFiles()
                .stream()
                .min(Comparator.comparingInt(UserFile::getDirectory))
                .orElseThrow(EmptySharedUserFilesException::new);
        shareInfo.setRootDirectory(uf.getDirectory());
        shareInfo.setCreatedTime(Timestamp.from(Instant.now()));
        shareInfoDao.addShareInfo(shareInfo);
        shareInfoDao.addUserFilesToShareInfoById(shareInfo.getId(), userFileIds);
    }

    private void recursivelySaveSharedUserFiles(UserFile userFile, List<Integer> userFileIds) {
        userFileIds.add(userFile.getId());
        UserFile userF = new UserFile();
        userF.setUsername(userFile.getUsername());
        userF.setDirectory(userFile.getId());
        userF.setUserFileStatus(UserFileStatus.NORMAL);
        userF.setFileStatus(FileStatus.NORMAL);
        userFileDao.findUserFiles(userF)
                .forEach(uf -> {
                    if (uf.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                        recursivelySaveSharedUserFiles(uf, userFileIds);
                    } else {
                        userFileIds.add(uf.getId());
                    }
                });
    }

    @Override
    public List<ShareInfo> getAllShareInfo(User user) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUsername(user.getUsername());
        //return shared user files those are normal file status
        return shareInfoDao.findShareInfo(shareInfo).stream().filter(si -> {
            int rootDirectory = si.getRootDirectory();
            List<UserFile> userFiles = si.getUserFiles().stream()
                    .filter(userFile -> (rootDirectory == userFile.getDirectory()
                                    && userFile.getFileStatus() == FileStatus.NORMAL
                                    && userFile.getUserFileStatus() == UserFileStatus.NORMAL))
                    .collect(Collectors.toList());
            si.setUserFiles(userFiles);
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public ShareInfo getShareInfoByIdAndAccessToken(String shareId, String accessToken, int directoryId) {
        //could be optimal when shareInfo has too many user files
        ShareInfo shareInfo = shareInfoDao.findShareInfoById(shareId);
        if (shareInfo == null) {
            return null;
        }
        if (shareInfo.getAccessToken() != null && !shareInfo.getAccessToken().equals(accessToken)) {
            throw new AccessTokenErrorException();
        }
        //if access token was right and expiry time is not expired or null(meaning forever),then return share info with user files
        if (shareInfo.getExpiryTime() == null || shareInfo.getExpiryTime().after(Timestamp.from(Instant.now()))) {
            shareInfo.setUserFiles(
                    shareInfo.getUserFiles()
                            .stream()
                            .filter(userFile -> (userFile.getDirectory() == directoryId
                                            && userFile.getFileStatus() == FileStatus.NORMAL
                                            && userFile.getUserFileStatus() == UserFileStatus.NORMAL
                            )).collect(Collectors.toList())
            );
            return shareInfo;
        } else {
            throw new ExpiredTimeException();
        }
    }

    @Override
    public void modifyShareInfoAccessTokenById(User user, String shareId, String accessToken) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        ShareInfo shareInfo = shareInfoDao.findShareInfoById(shareId);
        if (!shareInfo.getUsername().equals(user.getUsername())) {
            throw new ShareInfoOwnerException();
        }
        shareInfo.setCreatedTime(Timestamp.from(Instant.now()));
        shareInfo.setAccessToken(accessToken);
        shareInfoDao.updateShareInfoById(shareId, shareInfo);
    }

    @Override
    public void modifyShareInfoExpiryTimeById(User user, String shareId, Timestamp expiryTime) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        ShareInfo shareInfo = shareInfoDao.findShareInfoById(shareId);
        if (!shareInfo.getUsername().equals(user.getUsername())) {
            throw new ShareInfoOwnerException();
        }
        shareInfo.setCreatedTime(Timestamp.from(Instant.now()));
        shareInfo.setExpiryTime(expiryTime);
        shareInfoDao.updateShareInfoById(shareId, shareInfo);
    }

    @Override
    public void removeShareInfoById(User user, String shareId) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        ShareInfo shareInfo = shareInfoDao.findShareInfoById(shareId);
        if (!shareInfo.getUsername().equals(user.getUsername())) {
            throw new ShareInfoOwnerException();
        }
        ArrayList<Integer> userFileIds = new ArrayList<>();
        shareInfo.getUserFiles().forEach(
                userFile -> userFileIds.add(userFile.getId())
        );
        shareInfoDao.deleteUserFilesFromShareInfoById(shareId, userFileIds);
        shareInfoDao.deleteShareInfoById(shareId);
    }
}
