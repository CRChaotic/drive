package controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojo.ReportedFile;
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
}
