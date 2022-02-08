package controller;

import org.junit.Test;
import pojo.FileStatus;
import pojo.UserFile;
import pojo.UserFileStatus;
import utils.Identifier;

import java.io.*;

public class UserFileControllerTest {

    @Test
    public void createFileStorageDirectory() throws IOException {
        File dir = new File("./fileStorage");

        System.out.println(dir.getAbsolutePath());
        if(!dir.exists() && !dir.mkdir()){
            throw new RuntimeException("create fileStorage directory failed");
        }
        File uploadedFile = new File("./uploadedFile/x.txt");
        InputStream inputStream = new FileInputStream(uploadedFile);
        File tmpFile = File.createTempFile("tmp","",dir);
        tmpFile.deleteOnExit();
        OutputStream outputStream = new FileOutputStream(tmpFile);
        byte[] bytes = new byte[1024];
        int length;
        Identifier identifier = new Identifier("SHA3-256");
        while ((length = inputStream.read(bytes)) != -1){
            outputStream.write(bytes,0,length);
            identifier.read(bytes);
        }
        inputStream.close();
        outputStream.close();
        String fileId = identifier.getUniqueId();
        File file = new File(dir.getPath()+"/"+fileId);
        if(!file.exists() && tmpFile.renameTo(file)){
            UserFile userFile = new UserFile();
            userFile.setFileId(fileId);
            userFile.setFileStatus(FileStatus.NORMAL);
            userFile.setUserFileStatus(UserFileStatus.NORMAL);
            System.out.println(userFile);
        }else{
            System.out.println("file exists");
        }
    }
}
