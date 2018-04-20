import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.awt.Color;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Created by bobo on 11/4/17.
 */
public class png2jpg {
    public static void main(String[] args) throws Exception {
        String working_dir = "/Users/xiezhou/Documents/research_project/darknet-master/photos19390114/png/";
        File[] files = (new File(working_dir)).listFiles();
        ArrayList<String> filenames = new ArrayList<String>();
        for (File f : files) {
            if (f.getName().equals(".DS_Store"))
                continue;
            filenames.add(f.getName());
        }
        for(int i = 0; i < filenames.size();i++){
            String filename = filenames.get(i);
            String no_extention_name = filename_no_extention(filename);
            //read in image .png
            BufferedImage bufferedImage = ImageIO.read(new File(working_dir+filename));
            // create a blank, RGB, same width and height, and a white background
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
            //save to .jpg
            ImageIO.write(newBufferedImage, "jpg", new File(working_dir+no_extention_name+".jpg"));
        }
    }

    static public String filename_no_extention(String fullname){
        int i = fullname.indexOf('.');
        if(i== -1) {
            System.out.println("cannot file dot in filename, return");
            System.exit(0);
        }
        return fullname.substring(0,i);
    }
}
