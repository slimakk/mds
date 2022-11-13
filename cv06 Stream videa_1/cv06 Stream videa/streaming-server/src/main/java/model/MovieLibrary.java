package model;

import org.apache.commons.io.FilenameUtils;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class MovieLibrary extends ArrayList<Movie> {

    private String imagesDirectory, searchDirectory, keepSuffix;
    private int frameTime;

    public MovieLibrary(String imagesDirectory, String searchDirectory, String keepSuffix, int frameTime) throws IOException, JCodecException {
        super();
        this.frameTime = frameTime;
        this.imagesDirectory = imagesDirectory;
        this.searchDirectory = searchDirectory;
        this.keepSuffix = keepSuffix;

        fillMovies();
    }

    private void fillMovies() throws IOException, JCodecException {

        for(File file : discoverFiles(Path.of(searchDirectory), keepSuffix)){
            Picture frame = FrameGrab.getFrameFromFile(file, frameTime);
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(frame);
            String imageName = FilenameUtils.getBaseName(file.getName()) + "frame_" + frameTime + ".png";
            String imagePath = imagesDirectory + imageName;
            ImageIO.write(bufferedImage, "png", new File(imagePath));
            this.add(new Movie(file, imageName, file.getName()));
        }


    }

    private static List<File> discoverFiles(Path directory, String suffix) throws IOException {
        List<File> files = new ArrayList<>();

        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String filePath = String.valueOf(file);
                if(filePath.endsWith(suffix)) files.add(new File(filePath));
                return super.visitFile(file, attrs);
            }
        });
        return files;
    }

}
