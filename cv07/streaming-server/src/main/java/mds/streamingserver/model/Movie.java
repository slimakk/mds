package mds.streamingserver.model;

import java.io.File;

// Vytvořená třída movie pro uchovávání informací o nalezených mp4 souborech

public class Movie {

    File file = null;
    String image_name = null;
    String file_name = null;

    public Movie(File file, String image_name, String file_name){
        this.file = file;
        this.image_name = image_name;
        this.file_name = file_name;
    }


    public File getFile() {
        return file;
    }

    public String getImage_name() {
        return image_name;
    }

    public String getFile_name() { return file_name; }


}
