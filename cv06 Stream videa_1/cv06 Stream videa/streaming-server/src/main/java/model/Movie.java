package model;

import java.io.File;

public class Movie {
    private File file = null;
    private String image_name = null;
    private String file_name = null;

    public Movie(File file1, String image, String filename){
        this.file = file1;
        this.image_name = image;
        this.file_name = filename;
    }

    public File getFile() {
        return file;
    }

    public String getImage_name() {
        return image_name;
    }

    public String getFile_name() {
        return file_name;
    }
}
