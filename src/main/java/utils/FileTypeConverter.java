package utils;

import pojo.UserFile;

public interface FileTypeConverter {
    String DIRECTORY_TYPE = "dir";
    String convertToType(UserFile file);
}
