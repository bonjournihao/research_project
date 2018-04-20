import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bobo on 3/6/18.
 */
public class select_fewer_us {
    public static void main(String[] args) throws IOException, InterruptedException {
        //list_LLCN("/Users/xiezhou/Documents/research_project/newpaper_database/chroniclingamerica_list.txt");
        make_fewer();
    }
    private static int exist_in(String item, List<String> list) throws IOException {
        if(list.contains(item))
            return 1;
        else
            return 0;
    }

    private static void make_fewer() throws IOException {
        //list_LLCN("/Users/xiezhou/Documents/research_project/newpaper_database/chroniclingamerica_list.txt");
        List<String> LCCN_list = new ArrayList<>();
        FileReader fr = new FileReader(new File("/Users/xiezhou/Documents/research_project/newpaper_database/download_lists/us_list.txt"));
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(new File("/Users/xiezhou/Documents/research_project/newpaper_database/download_lists/us_list_fewer.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            String split_line[] = line.split("\\s");
            String LCCN = split_line[0];
            if(in_list(LCCN) == 1 && exist_in(LCCN,LCCN_list) == 0){
                fw.write(line+"\n");
                LCCN_list.add(LCCN);
                line = br.readLine();
                fw.write(line+"\n");
            }
            //skip one line
            else{
                line = br.readLine();
            }
        }
        fw.close();
        fr.close();
    }

    private static int in_list(String item) throws IOException {
        FileReader fr = new FileReader(new File("/Users/xiezhou/Documents/research_project/newpaper_database/selected_LCCN.txt"));
        String line;
        BufferedReader br = new BufferedReader(fr);
        while ((line = br.readLine()) != null) {
            String split_line[] = line.split("\\s");
            String LCCN = split_line[0];
            if(LCCN.equals(item))
                return 1;
        }
        return 0;
    }

    private static void list_LLCN(String filename) throws IOException {
        FileReader fr = new FileReader(new File(filename));
        FileWriter fw = new FileWriter(new File("/Users/xiezhou/Documents/research_project/newpaper_database/selected_LCCN.txt"));
        BufferedReader br = new BufferedReader(fr);
        String line;
        line = br.readLine();
        int count = 0;
        while ((line = br.readLine()) != null) {
            String split_line[] = line.split("\\s\\|\\s");
            String LCCN = split_line[3];
            String num = split_line[6];
            int num_issues = Integer.valueOf(num);
            if(num_issues >= 4000){
                fw.write(LCCN+"\n");
                count++;
            }
        }
        System.out.printf(String.valueOf(count));
        fr.close();
        fw.close();
    }
}
