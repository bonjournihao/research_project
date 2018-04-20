import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.exit;

//crop detections in images with probability higher that 0.3
/**
 * Created by bobo on 10/22/17.
 */
public class crop_image {
    public static void main(String[] args) throws Exception {
        //to be changed
        String working_dir = "/Users/xiezhou/Documents/research_project/";
        String folder_name = "test_smaller";



        String photo_dir2 = working_dir + "darknet-master/"+folder_name;
        String photo_dir = working_dir + "darknet-master/"+folder_name +"/";
        String coord_dir = working_dir + "darknet-master/"+folder_name+"_coord/";
        String crop_dir = working_dir + "darknet-master/"+folder_name+"_cropped/";
        String validation = working_dir + "darknet-master/results/comp4_det_test_pictaure.txt";

        create_directory(coord_dir);
        create_directory(crop_dir);

        //get coordinates for detections
        readValidation(validation, coord_dir);
        //crop images
        File[] files = (new File(photo_dir)).listFiles();
        Map<String,Integer> file_paths = new HashMap<>();
        for (File f : files) {
            if (f.getName().equals(".DS_Store"))
                continue;
            file_paths.put(f.getName(),1);
        }

        //make crop_dir
        crop(coord_dir, file_paths, photo_dir2,crop_dir);

    }

    //read from validation result + output coordinate file
    static public void readValidation(String filename, String save_dir) throws IOException {
        //create a folder to save image coordinates
        (new File(save_dir)).mkdir();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            String previous = "";
            int index = 0;
            //read validation file
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                //System.out.println(" part0:"+parts[0]+" part1:"+parts[1]+" part2:"+parts[2]+" part3:"+parts[3]+" part4:"+parts[4]+" part5:"+parts[5]);

                float prob = Float.parseFloat(parts[1]);
                //ignore detections with probability < 0.3
                if(prob < 0.3)
                    continue;

                String img_name = parts[0];
                if(img_name.equals(previous))
                    index++;
                else{
                    previous = img_name;
                    index = 0;
                }
                //create a folder for each test_image
                String dir_name = save_dir+img_name+"/";
                String file_name = save_dir+img_name+"/"+"pictaure"+Integer.toString(index);
                (new File(dir_name)).mkdir();
                BufferedWriter bw = new BufferedWriter(new FileWriter(file_name));
                bw.write(parts[2] + " " + parts[3] + " " + parts[4] + " " + parts[5]);
                bw.close();
            }
        }
    }

    static public void crop(String coord_dir, Map<String,Integer> img_names, String img_path, String crop_dir) throws IOException {
        (new File(crop_dir)).mkdir();

        File[] files = (new File(coord_dir)).listFiles();
        ArrayList<String> dir_names = new ArrayList<String>();
        for (File f : files) {
            if (f.getName().equals(".DS_Store"))
                continue;
            dir_names.add(f.getAbsolutePath());
        }
        int index = 0;
        for(int i = 0; i < dir_names.size(); i++){
            File f1 = new File(dir_names.get(i));
            String name1 = f1.getName();

            while(img_names.get(name1+".jpg") == null) {
                print(name1+".jpg");
                System.out.println("coord directory not matching image namesln");
                i++;
                f1 = new File(dir_names.get(i));
                name1 = f1.getName();
            }
            File folder = new File(dir_names.get(i));
            File[] listOfFiles = folder.listFiles();
            String inner_dir = folder.getAbsolutePath()+"/";
            for (int j = 0; j < listOfFiles.length; j++) {
                String filename_to_read = inner_dir + filename_no_extention(listOfFiles[j].getName());
                String filename_to_write = inner_dir + filename_no_extention(listOfFiles[j].getName()) + ".jpg";
                //read whole image
                File image_file = new File(img_path+"/"+name1+".jpg");

                BufferedImage img = ImageIO.read(image_file);
                BufferedImage rgbImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
                ColorConvertOp op = new ColorConvertOp(null);
                op.filter(img, rgbImage);

                //read coordinate info
                BufferedReader read_file = new BufferedReader(new FileReader(filename_to_read));
                String line = read_file.readLine();
                String[] parts = line.split(" ");
                float x = Float.parseFloat(parts[0]);
                float y = Float.parseFloat(parts[1]);
                float w = Float.parseFloat(parts[2]) - x;
                float h = Float.parseFloat(parts[3]) - y;

                //extend the box for 5%
                int new_width = (int)Math.round(w*1.05);
                int new_x = (int)Math.round(x - w*0.025);
                int new_height = (int)Math.round(h*1.05);
                int new_y = (int)Math.round(y - h*0.025);
                int image_width = img.getWidth();
                int image_height = img.getHeight();
                if(new_x < 0)
                    new_x = 0;
                if(new_y < 0)
                    new_y = 0;
                int x_diff = image_width - new_x-new_width;
                int y_diff = image_height - new_y-new_height;
                if(x_diff<0) {
                    //System.out.println("x_diff: "+x_diff+" imagewidth: " + image_width + " new_x: " + new_x + " new_width" + new_width);
                    new_x += Math.ceil(x_diff / 2);
                    new_width += x_diff;
                }
                if(y_diff<0){
                    //System.out.println("y_diff: "+y_diff+" imageheight: " + image_height + "new_y: " + new_y+ "new_height" + new_height);
                    new_y += Math.ceil(y_diff / 2);
                    new_height += y_diff;
                }

                read_file.close();
                //crop
                //System.out.println(filename_to_read+"\n x: "+new_x+" y: "+new_y+" w: "+new_width+" h: "+new_height+"\n");
                BufferedImage subImg = rgbImage.getSubimage(new_x,new_y,new_width,new_height);
                //write to file
                File outputfile = new File(filename_to_write);
                File output_list_file = new File(crop_dir+name1+"-"+filename_no_extention(listOfFiles[j].getName()) + ".jpg");
                ImageIO.write(subImg,"jpg", outputfile);
                ImageIO.write(subImg,"jpg",output_list_file);
            }
            index++;
        }
    }


    static public String filename_no_extention(String fullname){
        int i = fullname.indexOf('.');
        if(i== -1) {
            return fullname;
        }
        return fullname.substring(0,i);
    }
    private static void create_directory(String path){
        File theDir = new File(path);
        if (!theDir.exists()) {
            print("creating directory: " + theDir.getName());
            boolean result = false;

            try{
                theDir.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
            }
            if(result) {
                print("DIR created");
            }
        }
        else{
            print("folder "+theDir.getName()+" already exists");
        }
    }
    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

}
