import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bobo on 2/22/18.
 */

//dowdload us newspapers

public class saveImage_us1 {
    public static List<String> LCCN = new ArrayList<>();
    public static List<Integer> first_years_us = new ArrayList<>(); //add 1000 to the actual year
    public static List<Integer> last_years_us = new ArrayList<>();

    public static List<String> code_us = new ArrayList<>();
    public static List<List<String> > dates_us = new ArrayList<List<String>>();

    public static void main(String[] args) throws IOException, InterruptedException {
        String root_dir = "./";
        int start_i = 0;
        int start_j = 0;
        get_us_newspapers(root_dir,start_i, start_j);
        //list_us_valid_dates();
    }
    public static void list_us_valid_dates() throws IOException {
        String root_dir = "./";
        String filename = root_dir+"us_names_more.txt";

        FileReader fr = new FileReader(new File(filename));
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            String split_line[] = line.split("\\s");
            LCCN.add(split_line[0]);
            String first = split_line[1];
            String last = split_line[2];
            int first_year = Integer.valueOf(first);
            int last_year = Integer.valueOf(last);
            first_years_us.add(first_year);
            last_years_us.add(last_year);
        }
        fr.close();

        //setup chromedriver
        String url = "https://chroniclingamerica.loc.gov/lccn";
        System.setProperty("webdriver.chrome.driver", "chromedriver");

        File fout = new File("download_nz_ssh/download_lists/us_list.txt");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout)));

        //for each press

        //for each year, get valid dates
        for(int i = 0; i < LCCN.size(); i++){
            //write code+years
            String name = LCCN.get(i);
            String first = Integer.toString(first_years_us.get(i));
            String last = Integer.toString(last_years_us.get(i));
            String line2 = name + " " + first + " " + last + "\n";
            bw.write(line2);

            //write dates
            int num = last_years_us.get(i) - first_years_us.get(i) + 1;
            int current_year = first_years_us.get(i);
            for(int j = 0; j < num; j++) {
                WebDriver driver2 = new ChromeDriver();
                String year_page = url + "/" + LCCN.get(i) + "/issues/" + Integer.toString(current_year+j);
                driver2.get(year_page);

                //!!!!!
                //NOTE: record each valid date
                List<WebElement> published = driver2.findElements(By.xpath("//*[contains(@class,'single ')]"));
                //for each date, store validity
                for (WebElement each : published) {
                    String valid_string = each.findElement(By.xpath("./a")).getAttribute("href");
                    String year_month_day = valid_string.split("/")[5];
                    bw.write(year_month_day + " ");
                }
                driver2.quit();
            }
            bw.write("\n");
        }
        bw.close();
    }

    public static void get_us_newspapers(String root_dir, int start, int end) throws IOException, InterruptedException {
        //URL format https://chroniclingamerica.loc.gov/lccn/sn82014371/1864-07-14/ed-1/seq-1.jp2
        String list_name = root_dir + "download_nz_ssh/download_lists/us_list_fewer.txt";
        String us_dir = root_dir+"download_nz_ssh/download_papers/us_newspapers/";

        //read codes + dates into lists
        FileReader fr = new FileReader(new File(list_name));
        BufferedReader br = new BufferedReader(fr);
        String line;
        int line_odd = 1;
        while ((line = br.readLine()) != null) {
            if(line_odd == 1) { // odd line -> code
                line_odd = 0;
                String split_line[] = line.split("\\s");
                code_us.add(split_line[0]);
            }
            else{ //even line -> dates
                line_odd = 1;
                String split_line[] = line.split("\\s");
                List<String> temp_dates = Arrays.asList(split_line);
                dates_us.add(temp_dates);
            }
        }
        fr.close();
        print("Finshed reading us_list");

        String url_base = "https://chroniclingamerica.loc.gov/lccn/"; //need to add 1 to months "0"s infrontof month and date
        String url_end = "/ed-1/seq-1/";

        create_directory(us_dir);

        for(int i = start; i < code_us.size(); i++) {
            List<String> temp_string = dates_us.get(i);
            for (int j = 0; j < temp_string.size(); j++) { //temp_string.size()
                if(i == start && j < end)
                    continue;
                print("\n---CODE: "+code_us.get(i)+"--- i: "+i);
                String this_date = temp_string.get(j);
                String split_this[] = this_date.split("-");
                String year = split_this[0];
                String month = split_this[1];
                String day = split_this[2];

                String year_dir = us_dir + year + "/";
                String month_dir = year_dir + year + "-" + month + "/";
                String cur_dir = month_dir + year + "-" + month + "-" + day + "/";


                print("---year:%s, month:%s, date:%s--- j:%d", year, month, day,j);

                String current_url = url_base + code_us.get(i) + "/"+this_date + url_end;
                print("  Fetching %s...", current_url);

                String download_url = "/badurl/";
                //use chrome driver to get download url
                try{
                    download_url = us_news_url(current_url);
                    if(download_url == null){
                        print("\nEmpty page, skip!!\n");
                        continue;
                    }
                } catch(IOException e){
                    print("-->CAN'T get image DIM <--");
                }
                try{
                    //create folders
                    print("---creating folders--- ");
                    create_directory(year_dir);
                    create_directory(month_dir);
                    create_directory(cur_dir);

                    saveImg(download_url,code_us.get(i)+".jpg", cur_dir);
                } catch(IOException e){
                    print("-->BAD URL<--, skipping to next");
                    continue;
                }
            }
        }
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
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

    private static String us_news_url(String url) throws IOException, InterruptedException {
        //Document doc = Jsoup.connect("https://chroniclingamerica.loc.gov/lccn/sn83030483/1789-04-15/ed-1/seq-1/").get();
        //Elements links = doc.select("a#clip");
        //String linkHref = links.first().attr("href");

        // Optional, if not specified, WebDriver will search your path for chromedriver.
        System.setProperty("webdriver.chrome.driver", "/Users/xiezhou/Documents/chromedriver");

        WebDriver driver1 = new ChromeDriver();
        driver1.get(url);
        //String source = driver.getPageSource();
        //newspaper page
        if(driver1.findElements(By.id("clip")).size() == 0) {
            driver1.quit();
            return null;
        }
        WebElement clip_button = driver1.findElement(By.id("clip"));
        String clip_path = clip_button.getAttribute("href");
        driver1.quit();

        String image_clip_path = clip_path.split("/")[9];

        //clip_page
        /*
        WebDriver driver2 = new ChromeDriver();
        driver2.get(clip_path);
        WebElement download_button = driver2.findElement(By.className("clip_download"));
        String download_path = download_button.getAttribute("href");
        //System.out.println(download_path);
        driver2.quit();
        */

        return url+image_clip_path+".jpg";
    }

}
