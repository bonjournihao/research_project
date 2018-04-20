import java.io.File;
import java.io.IOException;

/**
 * Created by bobo on 3/2/18.
 */
public class move_around {
    public static void main(String[] args) throws IOException {
        String from = "/Users/xiezhou/Documents/research_project/newpaper_database/download_nz_ssh/download_papers/us_newspapers";
        String to = "/Users/xiezhou/Documents/research_project/newpaper_database/download_papers/us_newspapers";

        File[] years = (new File(from)).listFiles();
        //ArrayList<String> file_paths = new ArrayList<String>();
        for (File f1 : years) {
            if (f1.getName().equals(".DS_Store"))
                continue;
            String year = f1.getAbsolutePath();
            File year_dir = new File(year);
            File[] months = year_dir.listFiles();
            for (File f2 : months) {
                if (f2.getName().equals(".DS_Store"))
                    continue;
                String month = f2.getAbsolutePath();
                File month_dir = new File(month);
                File[] days = month_dir.listFiles();
                for (File f3 : days) {
                    if (f3.getName().equals(".DS_Store"))
                        continue;
                    String day = f3.getAbsolutePath();
                    File day_dir = new File(day);
                    File[] images = day_dir.listFiles();
                    for(File img : images){
                        create_directory(to+"/"+f1.getName());
                        create_directory(to+"/"+f1.getName()+"/"+f2.getName());
                        create_directory(to+"/"+f1.getName()+"/"+f2.getName()+"/"+f3.getName());
                        img.renameTo(new File(to+"/"+f1.getName()+"/"+f2.getName()+"/"+f3.getName(),img.getName()));
                    }
                }
            }

        }
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
