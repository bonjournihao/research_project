/**
 * Created by xiezhou on 6/27/17.
 */

//reference from:
//https://jsoup.org/cookbook/extracting-data/example-list-links

//dowdload french newspapers


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class saveImage_fr1 {
    public static List<String> code_fr = new ArrayList<>();
    public static List<List<String> > dates_fr = new ArrayList<List<String>>();


    public static void main(String[] args) throws IOException, InterruptedException {
        String root_dir = "download_nz_ssh/";
        int start_i = 0;
        int start_j = 0;
        get_french_news(root_dir,start_i, start_j);
    }

    public static void get_french_news(String root_dir, int start, int end) throws IOException {
        String list_name = root_dir + "download_lists/french_list.txt";
        String fr_dir = root_dir+"download_papers/french_newspapers/";

        //read codes + dates into lists
        FileReader fr = new FileReader(new File(list_name));
        BufferedReader br = new BufferedReader(fr);
        String line;
        int line_odd = 1;
        while ((line = br.readLine()) != null) {
            if(line_odd == 1) { // odd line -> code
                line_odd = 0;
                String split_line[] = line.split("\\s");
                code_fr.add(split_line[0]);
            }
            else{ //even line -> dates
                line_odd = 1;
                String split_line[] = line.split("\\s");
                List<String> temp_dates = Arrays.asList(split_line);
                dates_fr.add(temp_dates);
            }
        }
        fr.close();
        print("Finshed reading us_list");

        String url_base = "http://gallica.bnf.fr/ark:/12148/";

        create_directory(fr_dir);

        for(int i = start; i < code_fr.size(); i++) {
            List<String> temp_string = dates_fr.get(i);
            for (int j = 0; j < temp_string.size(); j++) {
                if(i == start && j < end)
                    continue;
                print("\n---Code: "+code_fr.get(i)+"--- i: "+i);
                String this_date = temp_string.get(j);
                String split_this[] = this_date.split("-");
                String year = split_this[0];
                String month = split_this[1];
                String day = split_this[2];

                String year_dir = fr_dir + year + "/";
                String month_dir = year_dir + year + "-" + month + "/";
                String cur_dir = month_dir + year + "-" + month + "-" + day + "/";


                print("---year:%s, month:%s, date:%s--- j:%d", year, month, day,j);

                String current_url = url_base + code_fr.get(i) + "/" + "date" + year+month+day;
                String real_url = fetch_real_url(current_url);

                //skip bad urls
                if(real_url == null || real_url.isEmpty()) {
                    print("   %s is a bad url\n", current_url);
                    continue;
                }
                try{
                    //create folders
                    print("---creating folders--- ");
                    create_directory(year_dir);
                    create_directory(month_dir);
                    create_directory(cur_dir);

                    print("Fetching %s...", current_url);
                    String img_url = real_url + "/f1.highres";

                    //save image to directory
                    saveImg(img_url, code_fr.get(i) + ".jpg", cur_dir);
                } catch(IOException e){
                    print("%s-->BAD URL<--, skipping to next",real_url);
                    continue;
                }
            }
        }
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String two_digits(int val){
        String temp;
        if(val > 9)
            temp = Integer.toString(val);
        else{
            temp = "0" + Integer.toString(val);
        }
        return temp;
    }
    private static void saveImg(String imageUrl, String filename, String dirname) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        File file = new File(dirname+"/"+filename);
        OutputStream os = new FileOutputStream(file);

        byte[] b = new byte[1024];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }
        is.close();
        os.close();
    }

    private static String fetch_real_url(String input_url) throws IOException {
        //connection to web page
        Document doc = Jsoup.connect(input_url).get();

        //count links
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        //Elements imports = doc.select("link[href]");
        //Elements cells = doc.select("[script]");

        //skip bad pages
        String bad = null;
        if (links.size() == 0 && media.size() == 0) {
            return bad;
        }

        //get url from METAdata
        String htmlString = doc.toString();
        Document doc_parsed = Jsoup.parse(htmlString);
        Elements meta = doc_parsed.select("META");

        int count = 0;
        for (Element n : meta) {
            count++;
        }
        if(count < 25){
            return bad;
        }

        Element eMETA = meta.get(4);

        String real_url = eMETA.attr("content");
        return real_url;
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
}

