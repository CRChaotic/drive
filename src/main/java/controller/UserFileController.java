package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pojo.User;
import pojo.UserFile;
import service.UserFileService;
import utils.Identifier;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

@RestController
@RequestMapping("/file")
public class UserFileController {
    private final UserFileService userFileService;
    private final Logger logger = Logger.getAnonymousLogger();

    public UserFileController(UserFileService userFileService){
        this.userFileService = userFileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UserFile> upload(@RequestParam("file") MultipartFile multipartFile, HttpSession session){
        String fileId;
        try {
            File dir = new File("./fileStorage");
            if(!dir.exists() && !dir.mkdir()){
                throw new RuntimeException("crap");
            }
            File tmpFile = File.createTempFile("tmp-","");
            InputStream in = multipartFile.getInputStream();
            OutputStream out = new FileOutputStream(tmpFile);
            byte[] bytes = new byte[1024];
            Identifier identifier = new Identifier("SHA3-256");
            while(in.read(bytes) != -1){
                out.write(bytes);
                identifier.read(bytes);
            }
            out.close();
            fileId = identifier.getUniqueId();
            User user = (User)session.getAttribute("user");
            UserFile userFile = new UserFile();
            userFile.setFileId(fileId);
            userFile.setUsername(user.getUsername());
            userFile.setType(multipartFile.getContentType());
            userFile.setSize(multipartFile.getSize());
            if(userFileService.saveUserFile(user,userFile) != null){
                in.reset();
                OutputStream output = new FileOutputStream(dir.getAbsolutePath()+"/"+fileId);
                in.transferTo(output);
                in.close();
                output.close();
            }
            if(tmpFile.delete())
                logger.log(Level.INFO,tmpFile.getName()+" was deleted");
            return new ResponseEntity<>(userFile, HttpStatus.CREATED);
        } catch (IOException e) {
           return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @GetMapping("/directory/{directoryId}")
    public ResponseEntity<List<UserFile>> getUserFilesByDirectory(@PathVariable int directoryId,HttpSession session){
        User user = (User)session.getAttribute("user");
        return new ResponseEntity<>(userFileService.getUserFilesByDirectoryId(user,directoryId),HttpStatus.OK);
    }
}
