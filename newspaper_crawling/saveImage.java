/**
 * Created by xiezhou on 6/27/17.
 */

//reference from:
//https://jsoup.org/cookbook/extracting-data/example-list-links

//dowdload new zealand newspapers

import java.util.Arrays;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class saveImage{
    public static List<String> code_us = new ArrayList<>();
    public static List<List<String> > dates_us = new ArrayList<List<String>>();
    public static List<String> code_fr = new ArrayList<>();
    public static List<Integer> first_years_fr = new ArrayList<>();
    public static List<Integer> last_years_fr = new ArrayList<>();
    public static List<String> code_nz = new ArrayList<>();
    public static List<List<String> > dates_nz = new ArrayList<List<String>>();

    public static List<String> LCCN = new ArrayList<>();
    public static List<Integer> first_years_us = new ArrayList<>(); //add 1000 to the actual year
    public static List<Integer> last_years_us = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        //get_french_news();
        //get_us_newspapers();
        int start_i = 0;
        int start_j = 0;
        get_newzealand_news(start_i,start_j);
        //list_newzealand_valid_dates();
        //list_france_valid_dates();
        //list_us_valid_dates();
        //get_newzealand_news();

    }

    public static void get_french_news() throws IOException {
        String root_dir = "download_nz_ssh/";
        String fr_dir = root_dir+"fr_newspapers/";
        String url_base = "http://gallica.bnf.fr/ark:/12148/"; //need to add 1 to months "0"s infrontof month and date
        //String url_end = "/ed-1/seq-1.pdf";

        new File(fr_dir).mkdir();
        //for each day from 1789-01-01 to 1943-12-31, create a folder

        //for(int n = 0; n < code_fr.size(); n++) {//for each newspaper
        for (int i = 1789; i <= 1943; i++) {
            String year_dir = fr_dir + i + "/";
            //a foler for a year
            new File(year_dir).mkdir();
            for (int j = 1; j <= 12; j++) {
                String month_dir = year_dir + i + "-" + two_digits(j) + "/";
                //a foler for a month
                new File(month_dir).mkdir();
                for (int k = 1; k <= 31; k++) {
                    //create a folder for a date
                    String this_date = i + "-" + two_digits(j) + "-" + two_digits(k);
                    String cur_dir = month_dir + this_date;
                    new File(cur_dir).mkdir();

                    //Date cur_date = new Date(cur_year - 1900, cur_month - 1, cur_day);

                    for (int m = 0; m < code_fr.size(); m++) {
                        if (i <= last_years_fr.get(m) && i >= first_years_fr.get(m)) { //if in range
                            //special cases
                            if(code_fr.get(m).equals("cb39294634r") && i == 1814 && j < 4)
                                continue;

                            //info
                            System.out.printf("\nyear: %d, month: %d, date: %d\n", i, j, k);

                            String current_url = url_base + code_fr.get(m) + "/" + "date" + Integer.toString(i) + String.format("%02d", j) + String.format("%02d", k);

                            //fetch image
                            String real_url = fetch_real_url(current_url);

                            //skip bad urls
                            if(real_url == null || real_url.isEmpty()) {
                                print("   %s is a bad url\n", current_url);
                                continue;
                            }
                            else {
                                print("Fetching %s...", current_url);
                                String img_url = real_url + "/f1.highres";
                                //save image to directory
                                saveImg(img_url, code_fr.get(m) + ".jpg", cur_dir);
                            }
                        }

                    }
                }
            }
        }
    }

    public static void get_us_newspapers() throws IOException, InterruptedException {
        //URL format https://chroniclingamerica.loc.gov/lccn/sn82014371/1864-07-14/ed-1/seq-1.jp2
        String root_dir = "download_nz_ssh/";
        String list_name = root_dir + "download_lists/us_list.txt";
        String us_dir = root_dir+"download_papers/us_newspapers/";

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

        for(int i = 0; i < code_us.size(); i++) {
            List<String> temp_string = dates_us.get(i);
            for (int j = 0; j < temp_string.size(); j++) {
                if(i == 0 && j < 0)
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
                //create folders
                print("---creating folders--- ");
                create_directory(year_dir);
                create_directory(month_dir);
                create_directory(cur_dir);

                print("---year:%s, month:%s, date:%s--- j:%d", year, month, day,j);

                String current_url = url_base + code_us.get(i) + "/"+this_date + url_end;
                print("  Fetching %s...", current_url);

                String download_url = "/badurl/";
                //use chrome driver to get download url
                try{
                    download_url = us_news_url(current_url);
                } catch(IOException e){
                    print("-->CAN'T get image DIM <--");
                }
                try{
                    saveImg(download_url,code_us.get(i)+".jpg", cur_dir);
                } catch(IOException e){
                    print("-->BAD URL<--, skipping to next");
                    continue;
                }
            }
        }
    }


    public static void get_newzealand_news(int start, int end) throws IOException{
        //https://paperspast.natlib.govt.nz/imageserver/newspapers/?oid=MT18770203.1.1&ext=gif
        //1839-1948
        String root_dir = "download_nz_ssh/";
        String list_name = root_dir + "download_lists/newzealand_list.txt";
        String nz_dir = root_dir+"download_papers/newzealand_newspaper/";

        //read codes + dates into lists
        FileReader fr = new FileReader(new File(list_name));
        BufferedReader br = new BufferedReader(fr);
        String line;
        int line_odd = 1;
        while ((line = br.readLine()) != null) {
            if(line_odd == 1) { // odd line -> code
                line_odd = 0;
                String split_line[] = line.split("\\s");
                code_nz.add(split_line[0]);
            }
            else{ //even line -> dates
                line_odd = 1;
                String split_line[] = line.split("\\s");
                List<String> temp_dates = Arrays.asList(split_line);
                dates_nz.add(temp_dates);
            }
        }
        fr.close();
        print("Finshed reading newzealand_list");

        String url_base = "https://paperspast.natlib.govt.nz/imageserver/newspapers/?oid=";
        String url_end = ".1.1&ext=gif";

        //create file directory
        create_directory(nz_dir);

        //for each valid day for each code, create a date folder if not exist
        for(int i = start; i < code_nz.size(); i++) {
            List<String> temp_string = dates_nz.get(i);
            for (int j = 0; j < temp_string.size(); j++) {
                if(i == start && j < end)
                    continue;
                print("\n---code: "+code_nz.get(i)+"--- i: "+i);
                String this_date = temp_string.get(j);
                String split_this[] = this_date.split("-");
                String year = split_this[0];
                String month = split_this[1];
                String day = split_this[2];

                String year_dir = nz_dir + year + "/";
                String month_dir = year_dir + year + "-" + month + "/";
                String cur_dir = month_dir + year + "-" + month + "-" + day + "/";

                print("---year: %s, month: %s, date: %s --- j:%d", year, month, day,j);
                //get downloading url
                String current_url = url_base + code_nz.get(i) + year + month + day + url_end;

                print("  Fetching %s...", current_url);
                //save image to directory

                try{
                    //create folders
                    print("---creating folders--- ");
                    create_directory(year_dir);
                    create_directory(month_dir);
                    create_directory(cur_dir);

                    saveImg(current_url, code_nz.get(i) + ".gif", cur_dir);

                } catch(IOException e){
                    print("-->BAD URL<--, skipping to next");
                    continue;
                }
                BufferedImage img = null;
                BufferedImage new_img = null;
                try {
                    File gif_file = new File(cur_dir+code_nz.get(i) + ".gif");
                    img = ImageIO.read(gif_file);
                    new_img = gif2jpg(img);

                    try {
                        // retrieve image
                        File outputfile = new File(cur_dir+code_nz.get(i) + ".jpg");
                        ImageIO.write(new_img, "jpg", outputfile);
                        new File(cur_dir+code_nz.get(i) + ".gif");
                    } catch (IOException e) {}
                    gif_file.delete();
                } catch (IOException e) {}
            }
        }
    }



    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }
    private static boolean in_range(Date val, Date min, Date max){
        int this_year = val.getYear()+1900;
        int this_month = val.getMonth()+1;
        int this_date = val.getDate();
        int min_year = min.getYear()+1900;
        int min_month = min.getMonth()+1;
        int min_date = min.getDate();
        int max_year = max.getYear()+1900;
        int max_month = max.getMonth()+1;
        int max_date = max.getDate();

        if(this_year < min_year || this_year > max_year)
            return false;
        //in year range
        if(min_year != max_year) {
            if (this_year > min_year && this_year < max_year) //in year range
                return true;
            else if (this_year == min_year) { //at min year
                if (this_month < min_month)
                    return false;
                else if (this_month > min_month)
                    return true;
                else {
                    if (this_date < min_date)
                        return false;
                    else
                        return true;
                }
            } else { // at max_year
                if (this_month > max_month)
                    return false;
                else if (this_month < max_month)
                    return true;
                else {
                    if (this_date > max_date)
                        return false;
                    else
                        return true;
                }
            }
        }
        //min_year == max_year
        else{
            System.out.print("min_year == max_year\n");  //min_year and max_year the same
            if(this_month < min_month || this_month > max_month)
                return false;
            if(min_month != max_month){
                if(this_month > min_month && this_month < max_month)
                    return true;
                else if(this_month == min_month){
                    if(this_date < min_date)
                        return false;
                    else
                        return true;
                }
                else{ //this_month == max_month
                    if(this_date > max_date)
                        return false;
                    else
                        return true;
                }
            }
            else{
                System.out.print("min_month == max_month\n");
                if(this_date < min_date || this_date > max_date)
                    return false;
                else
                    return true;
            }
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
    //return download_links for jpg
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
    private static int validate_url_links(String input_url) throws IOException {
        try {
            //connection to web page
            Document doc = Jsoup.connect(input_url).get();

            //count links
            Elements links = doc.select("a[href]");
            Elements media = doc.select("[src]");
            //Elements imports = doc.select("link[href]");
            //Elements cells = doc.select("[script]");

            //skip bad pages
            if (links.size() == 0 && media.size() == 0) {
                return -1;
            } else
                return 1;
        }catch (Exception e){
            return -1;
        }
    }

    //resize < 1000
    private static BufferedImage gif2jpg(BufferedImage bim) throws IOException {
        float imWidth = bim.getWidth();
        float imHeight = bim.getHeight();
        if(imWidth > 1000) {
            float temp_w = imWidth;
            float temp_h = imHeight;
            imHeight = (1000 / temp_w)*temp_h;
            imWidth = 1000;
        }

        //resize image
        BufferedImage new_im = new BufferedImage((int)imWidth, (int)imHeight, bim.getType());

        Graphics2D g = new_im.createGraphics();
        g.drawImage(bim,0,0,(int)imWidth, (int)imHeight,null);
        g.dispose();

        return new_im;
    }
    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }

    //use to generate an input list for us_list
    public static void temp0() throws IOException {
        String root_dir = "/Users/xiezhou/Documents/research_project/newpaper_database/";
        String filename = root_dir+"chroniclingamerica_list.txt";

        File fout = new File("/Users/xiezhou/Documents/research_project/newpaper_database/us_names.txt");
        BufferedWriter bw0 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout)));

        FileReader fr = new FileReader(new File(filename));
        BufferedReader br = new BufferedReader(fr);
        String line;
        line = br.readLine();
        while ((line = br.readLine()) != null) {
            String split_line[] = line.split("\\|");
            String code = new String(split_line[3].trim());
            String first[] = split_line[7].trim().split(",\\s|\\s+");
            String last[] = split_line[8].trim().split(",\\s|\\s+");
            int first_year = Integer.valueOf(first[2]);
            //int first_month = get_month(first[0])-1;
            //int first_date = Integer.valueOf(first[1]);
            int last_year = Integer.valueOf(last[2]);
            //int last_month = get_month(last[0])-1;
            //int last_date = Integer.valueOf(last[1]);

            //LCCN.add(code);
            //first_years_us.add(first_year);
            //last_years_us.add(last_year);
            String line2 = code + " " + Integer.toString(first_year) + " " + Integer.toString(last_year) + "\n";
            bw0.write(line2);
        }
        fr.close();
    }

    public static void list_us_valid_dates() throws IOException {
        String root_dir = "/Users/xiezhou/Documents/research_project/newpaper_database/";
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

        String url = "https://chroniclingamerica.loc.gov/lccn";
        System.setProperty("webdriver.chrome.driver", "/Users/xiezhou/Documents/chromedriver");

        File fout = new File("/Users/xiezhou/Documents/research_project/newpaper_database/us_list.txt");
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
                //if(i == 1887 && j < 16)
                    //continue;
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
    public static void list_france_valid_dates() throws IOException {
        String root_dir = "/Users/xiezhou/Documents/research_project/newpaper_database/";
        String filename = root_dir + "french_names.txt";
        FileReader fr = new FileReader(new File(filename));
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            String split_line[] = line.split("\\s");
            code_fr.add(split_line[0]);
            String first = split_line[1];
            String last = split_line[2];
            int first_year = Integer.valueOf(first);
            int last_year = Integer.valueOf(last);
            first_years_fr.add(first_year);
            last_years_fr.add(last_year);
        }
        fr.close();

        String url = "http://gallica.bnf.fr/ark:/12148";
        System.setProperty("webdriver.chrome.driver", "/Users/xiezhou/Documents/chromedriver");

        //WebDriver driver1 = new ChromeDriver();
        //driver1.get(url);

        File fout = new File("/Users/xiezhou/Documents/research_project/newpaper_database/french_list.txt");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout)));

        //write valid code+dates (need open links)
        //for each press
        for(int i = 37; i < code_fr.size(); i++){
            //write code+years
            String name = code_fr.get(i);
            String first = Integer.toString(first_years_fr.get(i));
            String last = Integer.toString(last_years_fr.get(i));
            String line2 = name + " " + first + " " + last + "\n";
            bw.write(line2);

            //write dates1
            int num = last_years_fr.get(i) - first_years_fr.get(i) + 1;
            int current_year = first_years_fr.get(i);
            for(int j = 0; j < num; j++) {
                if(i == 37 && j < 37)
                    continue;
                WebDriver driver2 = new ChromeDriver();
                String year_page = url + "/" + code_fr.get(i) + "/date" + Integer.toString(current_year+j);
                driver2.get(year_page);

                //!!!!!
                //NOTE: record each valid date
                List<WebElement> published = driver2.findElements(By.xpath("//*[@class='day-number ']"));
                //for each date, store validity
                for (WebElement each : published) {
                    String valid_string = each.findElement(By.xpath("./a")).getAttribute("href");
                    String year_month_day = fr_parseString(valid_string);
                    bw.write(year_month_day + " ");
                }
                driver2.quit();
            }
            bw.write("\n");
        }
        bw.close();
        //driver1.quit();
    }
    public static void list_newzealand_valid_dates() throws IOException {
        String website = "https://paperspast.natlib.govt.nz";
        String url = "https://paperspast.natlib.govt.nz/newspapers/all#year";
        System.setProperty("webdriver.chrome.driver", "/Users/xiezhou/Documents/chromedriver");

        WebDriver driver1 = new ChromeDriver();
        driver1.get(url);
        List<WebElement> name_list = driver1.findElements(By.xpath("//*[@class='table datatable dataTable no-footer']/tbody/tr"));
        List<WebElement> first_list = driver1.findElements(By.xpath("//*[@class='table datatable dataTable no-footer']/tbody/tr/td[3]"));
        List<WebElement> last_list = driver1.findElements(By.xpath("//*[@class='table datatable dataTable no-footer']/tbody/tr/td[4]"));
        List<WebElement> press_list = driver1.findElements(By.xpath("//*[@class='table datatable dataTable no-footer']/tbody/tr/td[1]/a"));

        File fout = new File("/Users/xiezhou/Documents/research_project/newpaper_database/newzealand_list.txt");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fout)));

        //write valid code+dates (need open links)
        //for each press
        for(int i = 0; i < name_list.size(); i++){  //oswcc >=1915
            //write code+years
            String name = name_list.get(i).getAttribute("data-publication");
            //code_nz.add(name);
            String first = first_list.get(i).getText();
            String last = last_list.get(i).getText();
            //first_years_nz.add(Integer.valueOf(first));
            //last_years_nz.add(Integer.valueOf(last));
            String line = name + " " + first + " " + last + "\n";
            bw.write(line);

            //write dates
            int num = Integer.valueOf(last) - Integer.valueOf(first) + 1;
            int current_year = Integer.valueOf(first);
            for(int j = 0; j < num; j++) {
                WebDriver driver2 = new ChromeDriver();
                String press_page = press_list.get(i).getAttribute("href")+"/"+Integer.toString(current_year+j);
                driver2.get(press_page);
                List<WebElement> published = driver2.findElements(By.xpath("//*[contains(@class,'table calendar calendar')]/tbody/tr/td[@class='text-center published']"));
                //for each date, store validity
                for (WebElement each : published) {
                    String valid_string = each.findElement(By.xpath("./span/a")).getAttribute("href");
                    String year_month_day = nz_parseString(valid_string);
                    bw.write(year_month_day + " ");
                }
                driver2.quit();
            }
            bw.write("\n");
        }
        bw.close();
        driver1.quit();
    }

    //return year-month-date
    private static String nz_parseString(String input){
        //input:： https://paperspast.natlib.govt.nz/newspapers/new-zealand-gazette-and-wellington-spectator/1839/9/6
        String[] result = input.split("/");
        String year = result[5];
        int month = Integer.valueOf(result[6]);
        int day = Integer.valueOf(result[7]);
        return year+"-"+two_digits(month)+"-"+two_digits(day);
    }

    private static String fr_parseString(String input){
        //input: http://gallica.bnf.fr/ark:/12148/cb326819451/date19080321
        String[] result = input.split("/");
        String good = result[6];
        String year = good.substring(4,8);
        String month = good.substring(8,10);
        String day = good.substring(10);
        return year+"-"+month+"-"+day;
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

