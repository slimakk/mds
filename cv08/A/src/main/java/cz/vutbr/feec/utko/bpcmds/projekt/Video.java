package cz.vutbr.feec.utko.bpcmds.projekt;

import java.io.File;

// Vytvořená třída movie pro uchovávání informací o nalezených mp4 souborech

public class Video {

    File file = null;
    String file_name = null;

    public Video(File file, String file_name){
        this.file = file;
        this.file_name = file_name;
    }

    public Video(){

    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


    public String getFile_name() { return file_name; }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
}
