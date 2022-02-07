package dao;

import pojo.ReportedFile;

import java.util.List;

public interface ReportedFileDao {
    void addReportedFile(ReportedFile reportedFile);
    ReportedFile findReportedFileById(Integer id);
    List<ReportedFile> findReportedFiles(ReportedFile reportedFile);
    void deleteReportedFileById(Integer id);
}
