package controller.form;

import pojo.FileStatus;

import java.util.List;

public class ModifyReportedFileStatusForm {
    private List<Integer> reportedFileIds;
    private FileStatus fileStatus;

    public List<Integer> getReportedFileIds() {
        return reportedFileIds;
    }

    public void setReportedUserFileIds(List<Integer> reportedFileIds) {
        this.reportedFileIds = reportedFileIds;
    }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }
}
