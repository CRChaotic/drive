package dao;

import org.junit.Test;
import pojo.*;
import utils.HttpContentTypeConverter;

import java.sql.Timestamp;
import java.time.Instant;

public class UserFileDaoTest {
    private static final UserFileDao userFileDao;
    private static final FileDao fileDao;
    private static final UserDao userDao;
    private static final String username = "ryan";
    private static final String fileId = "test_id";
    private static final String filename = "filename.pdf";
    static {
        userFileDao = DaoFactory.getDaoInstance(UserFileDao.class);
        fileDao = DaoFactory.getDaoInstance(FileDao.class);
        userDao = DaoFactory.getDaoInstance(UserDao.class);
    }

    protected static void addUserFileEnv(){
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setEmail("ryan@gmail.com");
        user.setRole(Role.NORMAL);
        user.setCapacity(10240L);
        user.setCreatedTime(Timestamp.from(Instant.now()));
        user.setUserStatus(UserStatus.NORMAL);
        userDao.addUser(user);
        File file = new File();
        file.setId(fileId);
        file.setSize(128L);
        file.setStatus(FileStatus.NORMAL);
        fileDao.addFile(file);
    }

    protected static int findUserFilesEnv(){
        UserFile userFile = new UserFile();
        userFile.setUsername(username);
        userFile.setFileId(fileId);
        userFile.setFilename(filename);
        userFile.setDirectory(1);
        userFile.setType(HttpContentTypeConverter.convertToContentType("pdf"));
        userFile.setCreatedTime(Timestamp.from(Instant.now()));
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFileDao.addUserFile(userFile);
        return userFile.getId();
    }

    @Test
    public void addUserFile(){
        addUserFileEnv();
        UserFile userFile = new UserFile();
        userFile.setUsername(username);
        userFile.setFileId(fileId);
        userFile.setFilename("filename.pdf");
        userFile.setType(HttpContentTypeConverter.convertToContentType("pdf"));
        userFile.setCreatedTime(Timestamp.from(Instant.now()));
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFileDao.addUserFile(userFile);
        System.out.println("userFileId:"+userFile.getId());
    }

    @Test
    public void findUserFiles(){
        addUserFileEnv();
        findUserFilesEnv();
        UserFile userFile = new UserFile();
        userFile.setFilename(filename);
        userFileDao.findUserFiles(userFile).forEach(System.out::println);
    }

    @Test
    public void wildcardFindUserFiles(){
        addUserFileEnv();
        findUserFilesEnv();
        UserFile userFile = new UserFile();
        userFile.setFilename("file");
        userFileDao.wildcardFindUserFiles(userFile).forEach(System.out::println);
    }

    @Test
    public void updateUserFileById(){
        addUserFileEnv();
        int id = findUserFilesEnv();
        UserFile userFile = new UserFile();
        userFile.setType("text/plain");
        userFileDao.updateUserFileById(id,userFile);
        System.out.println(userFileDao.findUserFileById(id));
    }

    @Test
    public void deleteUserFileById(){
        addUserFileEnv();
        int id = findUserFilesEnv();
        userFileDao.deleteUserFileById(id);
    }
}
