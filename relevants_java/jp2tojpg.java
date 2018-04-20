/**
 * Created by bobo on 2/6/18.
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class jp2tojpg {
    public static void main(String[] args) throws IOException {
        String filepath = "/Users/xiezhou/Downloads/";
        String filename = "sn85025905-18650419";
        convertImage(filepath,filename);

    }

    private static void convertImage(String path, String filename) throws IOException {
        try {
            File foundFile = new File(path + filename + ".jp2");
            BufferedImage buffer = ImageIO.read(foundFile);
            ImageIO.write(buffer, "jpg", new File(path + filename + ".jpg"));
            System.out.println("jpg file is generated");
        } catch (Exception e) {
            System.out.println("No file " + filename +".jp2 found");
        }

    }
}
