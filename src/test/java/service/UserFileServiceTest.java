package service;

import dao.*;
import org.junit.Test;
import pojo.*;
import service.implement.UserFileServiceImpl;
import service.implement.UserServiceImpl;
import utils.HttpContentTypeConverter;

import java.io.*;
import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class UserFileServiceTest {
    private static final UserFileService userFileService;

    static {
        UserFileDao userFileDao = DaoFactory.getDaoInstance(UserFileDao.class);
        FileDao fileDao = DaoFactory.getDaoInstance(FileDao.class);
        ReportedFileDao reportedFileDao = DaoFactory.getDaoInstance(ReportedFileDao.class);
        UserDao userDao = DaoFactory.getDaoInstance(UserDao.class);
        UserService userService = new UserServiceImpl(userDao);
        userFileService = new UserFileServiceImpl(userFileDao, fileDao, reportedFileDao, new HttpContentTypeConverter(), userService);
    }

    protected static UserFile saveUserFileEnv(User user, int directoryId, String fileId, String filename, FileStatus fileStatus) {
        UserFile userFile = new UserFile();
        userFile.setUsername(user.getUsername());
        userFile.setFileId(fileId);
        userFile.setFilename(filename);
        userFile.setDirectory(directoryId);
        userFile.setSize(0L);
        userFile.setCreatedTime(Timestamp.from(Instant.now()));
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFile.setFileStatus(fileStatus);
        try {
            File file = new File("C:\\E\\JavaWebApplications\\drive\\src\\test\\resources\\" + filename);
            InputStream in = new FileInputStream("C:\\E\\JavaWebApplications\\drive\\src\\test\\resources\\" + filename);
            OutputStream out = new FileOutputStream("C:\\E\\JavaWebApplications\\drive\\src\\test\\resources\\out\\" + fileId);
            long size = in.transferTo(out);
            userFile.setSize(size);
            userFileService.saveUserFile(user, userFile);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        } catch (IOException ioException) {
            System.out.println("Saving user file Failed");
        }
        return userFile;
    }

    protected static void reportUserFileEnv(User user) {
        UserFile uf1 = saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        UserFile uf2 = saveUserFileEnv(user, 0, "test_id2", "test_file2.txt", FileStatus.NORMAL);
        userFileService.reportUserFileByFileId(user, uf1.getFileId(), "no reason");
        userFileService.reportUserFileByFileId(user, uf2.getFileId(), "violent");
    }

    protected static void transformStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int length;
        while ((length = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    @Test
    public void saveUserFile() {
        User user = UserServiceTest.saveUserEnv();
        String filename = "test_file.txt";
        String fileId = "test_id";
        UserFile userFile = new UserFile();
        userFile.setUsername(user.getUsername());
        userFile.setFileId(fileId);
        userFile.setFilename(filename);
        userFile.setDirectory(0);
        userFile.setSize(1024L);
        userFile.setType(HttpContentTypeConverter.convertToContentType(userFile.getFilename()));
        userFile.setCreatedTime(Timestamp.from(Instant.now()));
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        try {
            InputStream in = new FileInputStream("C:\\E\\JavaWebApplications\\drive\\src\\test\\resources\\" + filename);
            OutputStream out = new FileOutputStream("C:\\E\\JavaWebApplications\\drive\\src\\test\\resources\\out\\" + fileId);
            transformStream(in, out);
            userFileService.saveUserFile(user, userFile);
        } catch (IOException ioException) {
            System.out.println("Saving user file Failed");
        }
    }

    @Test
    public void savaDirectory() {
        User user = UserServiceTest.saveUserEnv();
        userFileService.saveUserDirectory(user, 0, "a1");
    }

    @Test
    public void getUserFilesByDirectoryId() {
        User user = UserServiceTest.saveUserEnv();
        saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        saveUserFileEnv(user, 0, "test_id2", "test_file2.txt", FileStatus.NORMAL);
        userFileService.getUserFilesByDirectoryId(user, 0).forEach(System.out::println);
    }

    @Test
    public void getUserFilesByWildcardFilename() {
        User user = UserServiceTest.saveUserEnv();
        saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        saveUserFileEnv(user, 0, "test_id2", "test_file2.txt", FileStatus.NORMAL);
        userFileService.getUserFilesByWildcardFilename(user, "test_file").forEach(System.out::println);
    }

    @Test
    public void getUserFilesByWildcardType() {
        User user = UserServiceTest.saveUserEnv();
        saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        saveUserFileEnv(user, 0, "test_id2", "test_file2.txt", FileStatus.NORMAL);
        UserFile uf3 = saveUserFileEnv(user, 0, "test_id3", "test_file3.html", FileStatus.NORMAL);
        userFileService.getUserFilesByWildcardType(user, uf3.getType()).forEach(System.out::println);
    }

    @Test
    public void removeUserFileTemporarilyById() {
        User user = UserServiceTest.saveUserEnv();
        UserFile uf = saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        userFileService.removeTemporarilyUserFileById(user, uf.getId());
        userFileService.getRemovedTemporarilyUserFiles(user).forEach(System.out::println);
    }

    @Test
    public void removeTemporarilyUserDirectoryById() {
        User user = UserServiceTest.saveUserEnv();
        userFileService.saveUserDirectory(user, 0, "a1");
        List<UserFile> userFileList = userFileService.getUserFilesByDirectoryId(user, 0)
                .stream()
                .filter(userFile -> (userFile.getFilename().equals("a1")))
                .collect(Collectors.toList());
        saveUserFileEnv(user, userFileList.get(0).getId(), "test_id1", "test_file1.txt", FileStatus.NORMAL);
        saveUserFileEnv(user, userFileList.get(0).getId(), "test_id2", "test_file2.txt", FileStatus.NORMAL);
        saveUserFileEnv(user, 0, "test_id3", "test_file3.html", FileStatus.NORMAL);
        System.out.println(userFileList.get(0));
        userFileService.removeTemporarilyUserDirectoryById(user, userFileList.get(0).getId());
        userFileService.getRemovedTemporarilyUserFiles(user).forEach(System.out::println);
    }

    @Test
    public void getRemovedTemporarilyUserFiles() {
        User user = UserServiceTest.saveUserEnv();
        UserFile uf1 = saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        UserFile uf2 = saveUserFileEnv(user, 0, "test_id2", "test_file2.txt", FileStatus.NORMAL);
        UserFile uf3 = saveUserFileEnv(user, 0, "test_id3", "test_file3.html", FileStatus.NORMAL);
        userFileService.removeTemporarilyUserFileById(user, uf1.getId());
        userFileService.removeTemporarilyUserFileById(user, uf3.getId());
        userFileService.getRemovedTemporarilyUserFiles(user).forEach(System.out::println);
    }

    @Test
    public void removeUserFileById() {
        User user = UserServiceTest.saveUserEnv();
        UserFile uf1 = saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        userFileService.removeUserFileById(user, uf1.getId());
    }

    @Test
    public void removeUserDirectoryById() {
        User user = UserServiceTest.saveUserEnv();
        userFileService.saveUserDirectory(user, 0, "a1");
        List<UserFile> userFileList = userFileService.getUserFilesByDirectoryId(user, 0)
                .stream()
                .filter(userFile -> (userFile.getFilename().equals("a1")))
                .collect(Collectors.toList());
        saveUserFileEnv(user, userFileList.get(0).getId(), "test_id1", "test_file1.txt", FileStatus.NORMAL);
        saveUserFileEnv(user, userFileList.get(0).getId(), "test_id2", "test_file2.txt", FileStatus.NORMAL);
        saveUserFileEnv(user, 0, "test_id3", "test_file3.html", FileStatus.NORMAL);
        userFileService.removeUserDirectoryById(user, userFileList.get(0).getId());
    }

    @Test
    public void renameUserFileById() {
        User user = UserServiceTest.saveUserEnv();
        UserFile uf1 = saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        userFileService.renameUserFileById(user, uf1.getId(), "filename");
    }

    @Test
    public void reportUserFileByFileId() {
        User user = UserServiceTest.saveUserEnv();
        UserFile uf1 = saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        userFileService.reportUserFileByFileId(user, uf1.getFileId(), "no reason");
    }

    @Test
    public void restoreUserFileById() {
        User user = UserServiceTest.saveUserEnv();
        UserFile uf1 = saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        userFileService.removeTemporarilyUserFileById(user, uf1.getId());
        userFileService.getRemovedTemporarilyUserFiles(user).forEach(System.out::println);
        userFileService.restoreUserFileById(user, uf1.getId());
    }

    @Test
    public void restoreUserDirectoryById() {
        User user = UserServiceTest.saveUserEnv();
        userFileService.saveUserDirectory(user, 0, "a1");
        List<UserFile> userFileList = userFileService.getUserFilesByDirectoryId(user, 0)
                .stream()
                .filter(userFile -> (userFile.getFilename().equals("a1")))
                .collect(Collectors.toList());
        saveUserFileEnv(user, userFileList.get(0).getId(), "test_id1", "test_file1.txt", FileStatus.NORMAL);
        saveUserFileEnv(user, userFileList.get(0).getId(), "test_id2", "test_file2.txt", FileStatus.NORMAL);
        userFileService.removeTemporarilyUserDirectoryById(user, userFileList.get(0).getId());
        userFileService.getRemovedTemporarilyUserFiles(user).forEach(System.out::println);
        userFileService.restoreUserDirectoryById(user, userFileList.get(0).getId());
    }

    @Test
    public void modifyUserFileDirectory() {
        User user = UserServiceTest.saveUserEnv();
        userFileService.saveUserDirectory(user, 0, "a1");
        List<UserFile> userFileList = userFileService.getUserFilesByDirectoryId(user, 0)
                .stream()
                .filter(userFile -> (userFile.getFilename().equals("a1")))
                .collect(Collectors.toList());
        UserFile uf1 = saveUserFileEnv(user, 0, "test_id1", "test_file1.txt", FileStatus.NORMAL);
        userFileService.getUserFilesByDirectoryId(user, userFileList.get(0).getId()).forEach(System.out::println);
        userFileService.modifyUserFileDirectory(user, uf1.getId(), userFileList.get(0).getId());
        userFileService.getUserFilesByDirectoryId(user, userFileList.get(0).getId()).forEach(System.out::println);
    }
}
