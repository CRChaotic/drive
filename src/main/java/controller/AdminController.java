package controller;

import controller.form.ModifyReportedFileStatusForm;
import controller.form.ModifyUserRoleByUsernameForm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pojo.FileStatus;
import pojo.ReportedFile;
import pojo.Role;
import pojo.User;
import service.AdminService;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/reportedFiles")
    public List<ReportedFile> getAllReportedFiles(HttpSession session){
        User user = (User)session.getAttribute("user");
        return adminService.getAllReportedFiles(user);
    }

    @PatchMapping("/reportedFiles")
    public ResponseEntity<String> modifyReportedFileStatusByIds(@RequestBody ModifyReportedFileStatusForm reportedFileStatusForm, HttpSession session){
        List<Integer> reportedFileIds = reportedFileStatusForm.getReportedFileIds();
        FileStatus fileStatus = reportedFileStatusForm.getFileStatus();
        if(reportedFileIds.size() == 0){
            return new ResponseEntity<>("reportedFileIds parameter cannot be empty", HttpStatus.BAD_REQUEST);
        }
        User user = (User)session.getAttribute("user");
        reportedFileIds.forEach(id -> {
            ReportedFile reportedFile = adminService.getReportedFileById(user,id);
            if(reportedFile != null)
                adminService.modifyFileStatusById(user, reportedFile.getFileId(),fileStatus);
        });
        return new ResponseEntity<>(fileStatus.name(), HttpStatus.OK);
    }

    @PatchMapping ("/role")
    public ResponseEntity<String> modifyUserRoleByUsername(@RequestBody ModifyUserRoleByUsernameForm userRoleByUsernameForm, HttpSession session){
        String username = userRoleByUsernameForm.getUsername();
        Role role = userRoleByUsernameForm.getRole();
        User user = (User)session.getAttribute("user");
        adminService.modifyUserRoleByUsername(user,username,role);
        return new ResponseEntity<>("Modified user "+username+" role with "+role.name(),HttpStatus.OK);
    }
}
