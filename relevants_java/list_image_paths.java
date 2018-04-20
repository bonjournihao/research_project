import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by bobo on 7/30/17.
 *
 * OR
 *
 * Generate a list of the files to process:
 * find `pwd`/examples/images -type f -exec echo {} \; > examples/_temp/temp.txt
 */
public class list_image_paths {
    public static void main(String[] args) throws IOException {
        String working_dir = "/Users/xiezhou/Documents/research_project/darknet-master/test_smaller/";
        String base = "test_smaller/";
        File[] files = (new File(working_dir)).listFiles();

        ArrayList<String> filenames = new ArrayList<String>();

        for (File f : files) {
            if (f.getName().equals(".DS_Store"))
                continue;
            filenames.add(f.getName());
        }

        String train_filename = "darknet-master/test.txt";

        File myFoo = new File(train_filename);
        FileWriter fooWriter = new FileWriter(myFoo, false); //overwrite existing train.txt
        fooWriter.write("");
        fooWriter.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(train_filename,true));

        for(int i = 0; i < filenames.size();i++){
            writer.append(base);
            writer.append(filenames.get(i));
            writer.append("\n");
        }
        writer.close();
    }
}
