/**
 * Created by bobo on 09/06/17.
 */

//reference:
//https://stackoverflow.com/questions/11006496/select-an-area-to-capture-using-the-mouse
//selection of multiple rectangles instead of one

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.File;
import java.io.*;
import java.util.ArrayList;
import javax.imageio.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.geom.RectangularShape;


public class select_region {

    Rectangle captureRect;
    int count = -1;
    Point last_start;
    ArrayList<Rectangle> rectList = new ArrayList<Rectangle>();

    select_region(final BufferedImage screen) {
        final BufferedImage screenCopy = new BufferedImage(
                screen.getWidth(),
                screen.getHeight(),
                screen.getType());
        final JLabel screenLabel = new JLabel(new ImageIcon(screenCopy));
        JScrollPane screenScroll = new JScrollPane(screenLabel);

        screenScroll.setPreferredSize(new Dimension(500,700));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(screenScroll, BorderLayout.CENTER);

        final JLabel selectionLabel = new JLabel(
                "Drag a rectangle in the picture!");
        panel.add(selectionLabel, BorderLayout.SOUTH);

        repaint(screen, screenCopy);
        //screenLabel.repaint();

        screenLabel.addMouseMotionListener(new MouseMotionAdapter() {

            Point start = new Point();

            @Override
            public void mouseMoved(MouseEvent me) {
                start = me.getPoint();
                repaint(screen, screenCopy);
                selectionLabel.setText("Start Point: " + start);
                screenLabel.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent me) {
                boolean new_selection = false;
                if(last_start != start) {
                    new_selection = true;
                    last_start = start;
                }

                Point end = me.getPoint();
                captureRect = new Rectangle(start,
                        new Dimension(end.x-start.x, end.y-start.y));

                repaint(screen, screenCopy);
                screenLabel.repaint();
                selectionLabel.setText("Rectangle: " + captureRect);
                if(!new_selection) {
                    rectList.set(count,captureRect);
                }
                else{
                    count++;
                    rectList.add(captureRect);
                }
            }
        });

        JOptionPane.showMessageDialog(null, panel);

        //System.out.println("Rectangle of interest: " + captureRect + "\n");

        for(int i = 0; i < rectList.size();i++){
            System.out.println("Rectangle of interest: " + rectList.get(i));
        }
    }

    public void repaint(BufferedImage orig, BufferedImage copy) {
        Graphics2D g = copy.createGraphics();
        g.drawImage(orig,0,0, null);
        if (captureRect!=null) {
            g.setColor(Color.RED);
            g.draw(captureRect);
            g.setColor(new Color(255,255,255,150));
            g.fill(captureRect);
        }
        g.dispose();
    }

    public static void main(String[] args) throws Exception {
        String working_dir = "/Users/xiezhou/Documents/research_project/99photos/";
        File[] files = (new File(working_dir)).listFiles();

        ArrayList<String> filenames = new ArrayList<String>();

        for (File f : files) {
            if (f.getName().equals(".DS_Store"))
                continue;
            filenames.add(f.getName());
        }

        (new File("99labels")).mkdir();

        for (int j = 0; j <= 121; j++){
            String currentFile = filenames.get(j);

            File imageFile = new File(working_dir + currentFile);
            BufferedImage img = ImageIO.read(imageFile);

            String no_extention_name = filename_no_extention(currentFile);

            ArrayList<Rectangle> data_list = get_datalist(img);

            String label_filename = "99labels/" + no_extention_name + ".txt";
            write_to_file(data_list,img, label_filename);
        }
        //display_image(data_list, img);
    }

    static public ArrayList<Rectangle> get_datalist(BufferedImage img) throws IOException {
        ArrayList<Rectangle> data_list = new ArrayList<Rectangle>();

        //adjust newspaper size for selection
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screen_height = screenSize.getHeight();

        int set_ratio = (int)(img.getHeight()/screen_height);
        double real_ratio = (img.getHeight()/screen_height);

        int newWidth,newHeight;
        double ratio1,ratio2;

        if (set_ratio == 0){
            newWidth = (int)(img.getWidth()/real_ratio);//need to fix precision
            ratio1 = (double)img.getWidth()/(double)newWidth;
            newHeight = (int)(img.getHeight()/real_ratio);
            ratio2 = (double)img.getHeight()/(double)newHeight;
        }
        else {
            newWidth = img.getWidth() / set_ratio;
            ratio1 = img.getWidth() / newWidth;
            newHeight = img.getHeight() / set_ratio;
            ratio2 = img.getHeight() / newHeight;
        }

        Image tmp = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        final BufferedImage new_img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = new_img.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        //GUI -> select images
        final select_region my = new select_region(new_img);

        //get rectangles with right position and scale
        for(int i = 0; i < my.rectList.size();i++){
            Rectangle temp = my.rectList.get(i);
            Rectangle temp2 = new Rectangle((int)(temp.getX()*ratio1),(int)(temp.getY()*ratio2),(int)(temp.getWidth()*ratio1),(int)(temp.getHeight()*ratio2));
            data_list.add(temp2);
        }
        return data_list;
    }

    static public String filename_no_extention(String fullname){
        int i = fullname.indexOf('.');
        if(i== -1) {
            System.out.println("cannot file dot in filename, return");
            System.exit(0);
        }
        return fullname.substring(0,i);
    }

    //display image + selected region
    static public void display_image(ArrayList<Rectangle> data_list, BufferedImage img){
        //make a deepcopy of img
        //https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
        ColorModel cm = img.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = img.copyData(null);
        BufferedImage img_new = new BufferedImage(cm, raster, isAlphaPremultiplied, null);

        for(int i = 0; i < data_list.size();i++) {
            Graphics2D g = img_new.createGraphics();
            Rectangle rec = data_list.get(i);
            g.setColor(Color.BLACK);
            g.drawRect(rec.x,rec.y,rec.width,rec.height);
            g.setColor(new Color(255,255,255,200));
            g.fillRect(rec.x,rec.y,rec.width,rec.height);
            g.dispose();
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screen_height = screenSize.getHeight();

        int display_ratio = (int)(img_new.getHeight()/screen_height)+2;

        int display_width = img_new.getWidth()/display_ratio;
        int display_height = img_new.getHeight()/display_ratio;

        Image resized = img_new.getScaledInstance(display_width,display_height, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(display_width,display_height,  BufferedImage.TYPE_INT_ARGB);
        Graphics2D l = dimg.createGraphics();
        l.drawImage(resized, 0, 0, null);
        l.dispose();

        JFrame frame = new JFrame();
        ImageIcon icon = new ImageIcon(dimg);
        JLabel label = new JLabel(icon);
        frame.add(label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    //convert box coordinate format, write to label file
    static public void write_to_file(ArrayList<Rectangle> data_list, BufferedImage img,String filename) throws IOException {
        ArrayList<String> strings = new ArrayList<String>();
        int class_label = 0;
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for(int i = 0; i < data_list.size();i++){
            double img_w = img.getWidth();
            double img_h = img.getHeight();
            double w = data_list.get(i).getWidth()/img_w;
            double h = data_list.get(i).getHeight()/img_h;
            double x = data_list.get(i).getCenterX()/img_w;
            double y = data_list.get(i).getCenterY()/img_h;
            String temp_str = class_label+" "+ x + " " + y + " " + w + " " + h + "\n";
            strings.add(temp_str);
            writer.write(temp_str);
        }
        writer.close();
    }
}