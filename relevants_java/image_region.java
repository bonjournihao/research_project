import java.awt.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Created by bobo on 9/13/17.
 */
public class image_region {

    public static void main(String[] args) throws Exception {
        //get .txt and .jpg filenames
        String photo_dir = "/Users/xiezhou/Documents/research_project/99photos/";
        String label_dir = "/Users/xiezhou/Documents/research_project/99labels/";
        String composite_dir = "/Users/xiezhou/Documents/research_project/99image_region/";

        (new File(composite_dir)).mkdir();

        File[] files = (new File(photo_dir)).listFiles();

        ArrayList<String> filenames = new ArrayList<String>();

        for (File f : files) {
            if (f.getName().equals(".DS_Store"))
                continue;
            filenames.add(f.getName());
        }

        for (int j = 0; j < 122; j++){
            String currentFile = filenames.get(j);

            File imageFile = new File(photo_dir + currentFile);
            BufferedImage img = ImageIO.read(imageFile);

            String no_extention_name = filename_no_extention(currentFile);

            String label_filename = label_dir + no_extention_name + ".txt";

            ArrayList<Rectangle> data_list = file_to_list(label_filename,img);

            save_image(data_list,img, composite_dir + no_extention_name);
        }
    }

    static public ArrayList<Rectangle> file_to_list(String filename,  BufferedImage img) throws IOException{
        ArrayList<Rectangle> data_list = new ArrayList<Rectangle>();
        File f = new File(filename);
        BufferedReader b = new BufferedReader(new FileReader(f));
        String line = "";

        double img_w = img.getWidth();
        double img_h = img.getHeight();
        while ((line = b.readLine()) != null) {
            String[] tokens = line.split(" ");
            double x_center = Double.parseDouble(tokens[1])*img_w;
            double y_center = Double.parseDouble(tokens[2])*img_h;
            double region_width = Double.parseDouble(tokens[3])*img_w;
            double region_height = Double.parseDouble(tokens[4])*img_h;

            double x_left = x_center - region_width/2;
            double y_left = y_center - region_height/2;

            Rectangle temp = new Rectangle((int)x_left, (int)y_left, (int)region_width, (int)region_height);
            data_list.add(temp);
        }
        return data_list;
    }

    //save: image + selected region
    static public void save_image(ArrayList<Rectangle> data_list, BufferedImage img, String name) throws IOException {
        //make a deepcopy of img
        //https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
        ColorModel cm = img.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = img.copyData(null);
        BufferedImage img_new = new BufferedImage(cm, raster, isAlphaPremultiplied, null);

        for (int i = 0; i < data_list.size(); i++) {
            Graphics2D g = img_new.createGraphics();
            Rectangle rec = data_list.get(i);
            g.setColor(Color.BLACK);
            g.drawRect(rec.x, rec.y, rec.width, rec.height);
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(rec.x, rec.y, rec.width, rec.height);
            g.dispose();
        }

        //save composite image
        String new_image_name = name + "_region.jpg";
        File outputfile = new File(new_image_name);
        ImageIO.write(img_new,"jpg", outputfile);
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
