package service;

import dao.*;
import org.junit.Test;
import pojo.FileStatus;
import pojo.ShareInfo;
import pojo.User;
import pojo.UserFile;
import service.implement.ShareFileServiceImpl;
import service.implement.UserFileServiceImpl;
import service.implement.UserServiceImpl;
import utils.HttpContentTypeConverter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShareFileServiceTest {
    private static final ShareFileService shareFileService;
    private static final UserFileService userFileService;
    static {
        ShareInfoDao shareInfoDao = DaoFactory.getDaoInstance(ShareInfoDao.class);
        UserFileDao userFileDao = DaoFactory.getDaoInstance(UserFileDao.class);
        UserDao userDao = DaoFactory.getDaoInstance(UserDao.class);
        ReportedFileDao reportedFileDao = DaoFactory.getDaoInstance(ReportedFileDao.class);
        UserService userService = new UserServiceImpl(userDao);
        shareFileService = new ShareFileServiceImpl(shareInfoDao,userFileDao,userService);
        FileDao fileDao = DaoFactory.getDaoInstance(FileDao.class);
        userFileService = new UserFileServiceImpl(userFileDao,fileDao,reportedFileDao,new HttpContentTypeConverter(), userService);
    }

    @Test
    public void saveShareInfo(){
        User user = UserServiceTest.saveUserEnv();
        userFileService.saveUserDirectory(user,0,"a1");
        List<UserFile> userFileList = userFileService.getUserFilesByDirectoryId(user,0)
                .stream()
                .filter(userFile -> (userFile.getFilename().equals("a1")))
                .collect(Collectors.toList());
        UserFile uf1 = UserFileServiceTest.saveUserFileEnv(user,userFileList.get(0).getId(), "test_id1","test_file1.txt", FileStatus.NORMAL);
        UserFile uf2 = UserFileServiceTest.saveUserFileEnv(user,0, "test_id2","test_file2.txt", FileStatus.NORMAL);
        ShareInfo shareInfo = new ShareInfo();
        ArrayList<UserFile> userFiles = new ArrayList<>();
        userFiles.add(userFileList.get(0));
        userFiles.add(uf2);
        shareInfo.setUserFiles(userFiles);
        shareInfo.setAccessToken("12345678");
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
        shareFileService.saveShareInfo(user,shareInfo);
    }

    @Test
    public void getAllUserFileInfo(){
        User user = UserServiceTest.saveUserEnv();
        UserFile uf = UserFileServiceTest.saveUserFileEnv(user,0, "test_id","test_file.txt", FileStatus.BLOCKED);
        UserFile uf1 = UserFileServiceTest.saveUserFileEnv(user,0, "test_id1","test_file1.txt", FileStatus.NORMAL);
        ArrayList<UserFile> userFiles = new ArrayList<>();
        userFiles.add(uf);
        userFiles.add(uf1);
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUserFiles(userFiles);
        shareInfo.setAccessToken("12345678");
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));
        shareFileService.saveShareInfo(user,shareInfo);
        shareFileService.getAllShareInfo(user).forEach(shareInfo1 -> {
            System.out.println(shareInfo1);
            shareInfo1.getUserFiles().forEach(System.out::println);
        });
    }

    @Test
    public void getShareInfoByIdAndAccessToken(){
        User user = UserServiceTest.saveUserEnv();
        userFileService.saveUserDirectory(user,0,"a1");
        List<UserFile> userFileList = userFileService.getUserFilesByDirectoryId(user,0)
                .stream()
                .filter(userFile -> (userFile.getFilename().equals("a1")))
                .collect(Collectors.toList());
        UserFile uf1 = UserFileServiceTest.saveUserFileEnv(user,userFileList.get(0).getId(), "test_id1","test_file1.txt", FileStatus.NORMAL);
        UserFile uf2 = UserFileServiceTest.saveUserFileEnv(user,0, "test_id2","test_file2.txt", FileStatus.NORMAL);
        ShareInfo shareInfo = new ShareInfo();
        ArrayList<UserFile> userFiles = new ArrayList<>();
        //directory was including uf1 user file
        userFiles.add(userFileList.get(0));
        userFiles.add(uf2);
        shareInfo.setUserFiles(userFiles);
        shareInfo.setAccessToken("12345678");
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
        shareFileService.saveShareInfo(user,shareInfo);
        shareFileService.getShareInfoByIdAndAccessToken(shareInfo.getId(), shareInfo.getAccessToken(),uf2.getDirectory()).getUserFiles().forEach(System.out::println);
    }

    @Test
    public void getSharedUserFileByUserFileId(){
        User user = UserServiceTest.saveUserEnv();
        userFileService.saveUserDirectory(user,0,"a1");
        List<UserFile> userFileList = userFileService.getUserFilesByDirectoryId(user,0)
                .stream()
                .filter(userFile -> (userFile.getFilename().equals("a1")))
                .collect(Collectors.toList());
        UserFile uf1 = UserFileServiceTest.saveUserFileEnv(user,userFileList.get(0).getId(), "test_id1","test_file1.txt", FileStatus.NORMAL);
        UserFile uf2 = UserFileServiceTest.saveUserFileEnv(user,0, "test_id2","test_file2.txt", FileStatus.NORMAL);
        ShareInfo shareInfo = new ShareInfo();
        ArrayList<UserFile> userFiles = new ArrayList<>();
        //directory was including uf1 user file
        userFiles.add(userFileList.get(0));
        userFiles.add(uf2);
        shareInfo.setUserFiles(userFiles);
        shareInfo.setAccessToken("12345678");
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
        shareFileService.saveShareInfo(user,shareInfo);
        System.out.println(shareFileService.getSharedUserFileByUserFileId(shareInfo.getId(),shareInfo.getAccessToken(),uf1.getId()));
    }

    @Test
    public void modifyShareInfoAccessTokenById(){
        User user = UserServiceTest.saveUserEnv();
        UserFile uf = UserFileServiceTest.saveUserFileEnv(user,0, "test_id","test_file.txt", FileStatus.BLOCKED);
        UserFile uf1 = UserFileServiceTest.saveUserFileEnv(user,0, "test_id1","test_file1.txt", FileStatus.NORMAL);
        ArrayList<UserFile> userFiles = new ArrayList<>();
        userFiles.add(uf);
        userFiles.add(uf1);
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUserFiles(userFiles);
        shareInfo.setAccessToken("12345678");
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));
        shareFileService.saveShareInfo(user,shareInfo);
        shareFileService.modifyShareInfoAccessTokenById(user,shareInfo.getId(),"123");
        shareFileService.getAllShareInfo(user).forEach(System.out::println);
    }

    @Test
    public void modifyShareInfoExpiryTimeById(){
        User user = UserServiceTest.saveUserEnv();
        UserFile uf = UserFileServiceTest.saveUserFileEnv(user,0, "test_id","test_file.txt", FileStatus.BLOCKED);
        UserFile uf1 = UserFileServiceTest.saveUserFileEnv(user,0, "test_id1","test_file1.txt", FileStatus.NORMAL);
        ArrayList<UserFile> userFiles = new ArrayList<>();
        userFiles.add(uf);
        userFiles.add(uf1);
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUserFiles(userFiles);
        shareInfo.setAccessToken("12345678");
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));
        shareFileService.saveShareInfo(user,shareInfo);
        shareFileService.modifyShareInfoExpiryTimeById(user,shareInfo.getId(),Timestamp.valueOf(LocalDateTime.now().plusDays(7L)));
    }

    @Test
    public void removeShareInfoById(){
        User user = UserServiceTest.saveUserEnv();
        UserFile uf = UserFileServiceTest.saveUserFileEnv(user,0, "test_id","test_file.txt", FileStatus.NORMAL);
        UserFile uf1 = UserFileServiceTest.saveUserFileEnv(user,0, "test_id1","test_file1.txt", FileStatus.NORMAL);
        ArrayList<UserFile> userFiles = new ArrayList<>();
        userFiles.add(uf);
        userFiles.add(uf1);
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUserFiles(userFiles);
        shareInfo.setAccessToken("12345678");
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));
        shareFileService.saveShareInfo(user,shareInfo);
        shareFileService.removeShareInfoById(user,shareInfo.getId());
    }
}
