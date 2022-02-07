package dao;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import pojo.File;
import pojo.FileStatus;

import java.io.IOException;
import java.io.InputStream;

public class FileDaoTest {
    private static final String resource = "mybatis-config.xml";
    private static final FileDao fileDao;

    static {
        fileDao = DaoFactory.getDaoInstance(FileDao.class);
    }

    protected static File findFileEnv() {
        File file = new File();
        file.setId("test_id1");
        file.setSize(128L);
        file.setStatus(FileStatus.NORMAL);
        fileDao.addFile(file);
        return file;
    }

    @Test
    public void addFile() {
        File file = new File();
        file.setId("test_id1");
        file.setSize(128L);
        file.setStatus(FileStatus.NORMAL);
        fileDao.addFile(file);
    }

    @Test
    public void findFileById() {
        File file = findFileEnv();
        System.out.println(fileDao.findFileById(file.getId()));
    }

    @Test
    public void findFiles() {
        findFileEnv();
        File f = new File();
        f.setStatus(FileStatus.NORMAL);
        fileDao.findFiles(f).forEach(System.out::println);
    }

    @Test
    public void updateFileById() {
        String fileId = "test_id1";
        File file = new File();
        file.setStatus(FileStatus.BLOCKED);
        fileDao.updateFileById(fileId, file);
        System.out.println(fileDao.findFileById(fileId));
    }

    @Test
    public void deleteFileById() {
        String fileId = "test_id1";
        fileDao.deleteFileById(fileId);
        System.out.println(fileDao.findFileById(fileId));
    }
}
