package dao;

import org.junit.Test;
import pojo.File;
import pojo.ReportedFile;
import pojo.User;

import java.util.List;

public class ReportedFileDaoTest {
    protected static ReportedFileDao reportedFileDao;
    static {
        reportedFileDao = DaoFactory.getDaoInstance(ReportedFileDao.class);
    }

    protected static ReportedFile findReportedFileEnv(){
        File file = FileDaoTest.findFileEnv();
        User user = UserDaoTest.findUserEnv();
        ReportedFile reportedFile = new ReportedFile();
        reportedFile.setFileId(file.getId());
        reportedFile.setUsername(user.getUsername());
        reportedFile.setReason("no reason");
        reportedFileDao.addReportedFile(reportedFile);
        return reportedFile;
    }

    @Test
    public void addReportedFile(){
        File file = FileDaoTest.findFileEnv();
        User user = UserDaoTest.findUserEnv();
        ReportedFile reportedFile = new ReportedFile();
        reportedFile.setFileId(file.getId());
        reportedFile.setUsername(user.getUsername());
        reportedFile.setReason("no reason");
        reportedFileDao.addReportedFile(reportedFile);
        System.out.println("Reported file id "+reportedFile.getId());
    }

    @Test
    public void findReportedFileById(){
        ReportedFile reportedFile = findReportedFileEnv();
        System.out.println(reportedFileDao.findReportedFileById(reportedFile.getId()));
    }

    @Test
    public void findReportedFiles(){
        ReportedFile reportedFile = findReportedFileEnv();
        reportedFileDao.findReportedFiles(reportedFile).forEach(System.out::println);
    }

    @Test
    public void deleteReportedFileById(){
        ReportedFile reportedFile = findReportedFileEnv();
        reportedFileDao.deleteReportedFileById(reportedFile.getId());
    }
}
