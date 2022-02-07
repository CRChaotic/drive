package dao;

import org.junit.Test;
import pojo.ShareInfo;
import pojo.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShareInfoDaoTest {
    private static final ShareInfoDao shareInfoDao;
    private static final String username = "ryan";
    static {
        shareInfoDao = DaoFactory.getDaoInstance(ShareInfoDao.class);
    }

    protected static String findShareInfoEnv(String token){
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUsername(username);
        shareInfo.setId(UUID.randomUUID().toString());
        shareInfo.setAccessToken(token);
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
        shareInfoDao.addShareInfo(shareInfo);
        return shareInfo.getId();
    }

    @Test
    public void addShareInfo(){
        User user = UserDaoTest.findUserEnv();
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUsername(user.getUsername());
        shareInfo.setId(UUID.randomUUID().toString());
        shareInfo.setAccessToken("12345678");
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
        shareInfoDao.addShareInfo(shareInfo);
    }

    @Test
    public void addUserFilesToShareInfoById(){
        int userFileId = UserFileDaoTest.findUserFilesEnv();
        String id = findShareInfoEnv("1234568");
        List<Integer> userFileIds = new ArrayList<>();
        userFileIds.add(userFileId);
        shareInfoDao.addUserFilesToShareInfoById(id,userFileIds);
    }

    @Test
    public void deleteUserFilesFromShareInfoById(){
        int userFileId = UserFileDaoTest.findUserFilesEnv();
        String id = findShareInfoEnv("1234568");
        List<Integer> userFileIds = new ArrayList<>();
        userFileIds.add(userFileId);
        shareInfoDao.addUserFilesToShareInfoById(id,userFileIds);
        shareInfoDao.deleteUserFilesFromShareInfoById(id,userFileIds);
    }

    @Test
    public void findShareInfo(){
        UserFileDaoTest.addUserFileEnv();
        UserFileDaoTest.findUserFilesEnv();
        String id = findShareInfoEnv("12345678");
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setId(id);
        System.out.println(shareInfoDao.findShareInfo(shareInfo));
    }

    @Test
    public void updateShareInfoById(){
        UserFileDaoTest.addUserFileEnv();
        int userFileId = UserFileDaoTest.findUserFilesEnv();
        String id = findShareInfoEnv("12345678");
        List<Integer> userFileIds = new ArrayList<>();
        userFileIds.add(userFileId);
        shareInfoDao.addUserFilesToShareInfoById(id,userFileIds);
        //ready for updating share info
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusDays(3)));
        shareInfoDao.updateShareInfoById(id,shareInfo);
        System.out.println(shareInfoDao.findShareInfoById(id));
        System.out.println(shareInfoDao.findShareInfoById(id).getUserFiles().get(0));
    }

    @Test
    public void deleteShareInfoById(){
        UserFileDaoTest.addUserFileEnv();
        int userFileId = UserFileDaoTest.findUserFilesEnv();
        String id = findShareInfoEnv("12345678");
        List<Integer> userFileIds = new ArrayList<>();
        userFileIds.add(userFileId);
        shareInfoDao.addUserFilesToShareInfoById(id,userFileIds);
        shareInfoDao.deleteShareInfoById(id);
    }
}
