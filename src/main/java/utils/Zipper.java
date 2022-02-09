package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {
    private String name;
    private long size;
    private ZipOutputStream zipOut;
    private final byte[] buffer =  new byte[8192];

    public Zipper(String name) throws IOException {
        this.name = name;
        File zipFile = new File(this.name);
        if (zipFile.createNewFile()) {
            this.zipOut = new ZipOutputStream(new FileOutputStream(this.name));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        size = new File(this.name).length();
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void addEntry(String filenameInZip, String path) {
        try {
            ZipEntry zipEntry = new ZipEntry(filenameInZip);
            FileInputStream fileIn = new FileInputStream(path);
            this.zipOut.putNextEntry(zipEntry);
            int length;
            while (true) {
                length = fileIn.read(buffer);
                if (length > -1) {
                    this.zipOut.write(buffer, 0, length);
                } else {
                    break;
                }
            }
            fileIn.close();
            this.zipOut.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void done() {
        try {
            this.zipOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
