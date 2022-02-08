package controller;

import org.junit.Test;
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
        File tmpFile = File.createTempFile("tmp","",new File("./uploadedFile"));
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
        if(tmpFile.renameTo(file)){
            UserFile userFile = new UserFile();
            userFile.setFileId(fileId);
            userFile.setUserFileStatus(UserFileStatus.NORMAL);
            if(tmpFile.delete())
                System.out.println(userFile);
        }
    }
}
