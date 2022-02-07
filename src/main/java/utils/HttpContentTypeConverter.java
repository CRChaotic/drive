package utils;

import org.springframework.stereotype.Component;
import pojo.UserFile;

import java.util.HashMap;
import java.util.Map;

@Component("fileTypeConverter")
public class HttpContentTypeConverter implements FileTypeConverter{
    private static final Map<String,String> contentTypeMap = new HashMap<>();
    static {
        contentTypeMap.put("pdf","application/pdf");
        contentTypeMap.put("mp3","audio/mp3");
        contentTypeMap.put("gif","image/gif");
        contentTypeMap.put("jpg","image/jpeg");
        contentTypeMap.put("png","image/png");
        contentTypeMap.put("css","text/css");
        contentTypeMap.put("html","text/html");
        contentTypeMap.put("txt","text/plain");
        contentTypeMap.put("mp4","video/mp4");
    }

    public static String convertToContentType(String suffix){
        return contentTypeMap.get(suffix);
    }

    @Override
    public String convertToType(UserFile userFile) {
        String filename = userFile.getFilename();
        int dot = filename.lastIndexOf(".");
        if(dot != -1){
            String suffix = filename.substring(dot+1);
            return convertToContentType(suffix);
        }else{
            return null;
        }
    }
}
