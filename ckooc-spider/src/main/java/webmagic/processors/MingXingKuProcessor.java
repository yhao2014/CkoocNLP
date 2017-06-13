package webmagic.processors;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import webmagic.pipelines.BasePipeline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 从"明星库"网站爬取明星资料
 * URL: http://www.mingxingku.com/star
 *
 * 爬取字段:
 *
 * Created by yhao on 2017/3/27.
 */
public class MingXingKuProcessor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me()
//            .setCharset("gbk")
            .setRetryTimes(5)
            .setSleepTime(300)
            .setAcceptStatCode(new HashSet<Integer>(){{add(404); add(200);}})
            .setCycleRetryTimes(6)
            .setUserAgent(UA);

    private static Long count = 0L;
    private static Long sum = 0L;

    @Override
    public void process(Page page) {
        if (page.getStatusCode() != 404) {
            String table = "//div[@class='intro']/table/tbody";

            HashMap<String, String> fieldsMap = new HashMap<>();

            List<String> fields = page.getHtml().xpath(table + "/tr/td/span/text()").all();
            List<String> values = page.getHtml().xpath(table + "/tr/td/text()").all();
            fields.addAll(page.getHtml().xpath(table + "/tr/th/span/text()").all());
            values.addAll(page.getHtml().xpath(table + "/tr/th/text()").all());

            if (fields.size() == values.size()) {
                for (int i = 0; i < fields.size(); i++) {
                    String field = fields.get(i).replaceAll("\\s+", "").replaceAll("\\t+", "").replaceAll(" ", "");
                    if (field.contains("：")) fieldsMap.put(field.substring(0, field.indexOf("：")), values.get(i));
                    else if (field.contains(":")) fieldsMap.put(field.substring(0, field.indexOf(":")), values.get(i));
                    else fieldsMap.put(field, values.get(i));
                }
            }

            String birthday = page.getHtml().xpath("//div[@class='avatar']/p/span/text()").get();
            fieldsMap.put("生日", birthday);

            List<String> describeTitles = page.getHtml().xpath("//div[@class='subtitle']/h3/text()").all();
            List<String> describeList = page.getHtml().xpath("//div[@class='intro']").all();

            List<String> describes = new LinkedList<>();
            for (int i = 1; i < describeTitles.size(); i++) {
                List<String> temp = new Html(describeList.get(i)).xpath("//p/text()").all();
                temp.add(0, describeTitles.get(i));

                describes.addAll(temp);
            }


            page.putField("name_ch", fieldsMap.getOrDefault("中文名", ""));
            page.putField("name_en", fieldsMap.getOrDefault("外文名", ""));
            page.putField("name_alias", fieldsMap.getOrDefault("别名", ""));
            page.putField("nationality", fieldsMap.getOrDefault("国籍", ""));
            page.putField("nation", fieldsMap.getOrDefault("民族", ""));
            page.putField("birthplace", fieldsMap.getOrDefault("出生地", ""));
            page.putField("birthday", fieldsMap.getOrDefault("生日", ""));
            page.putField("profession", fieldsMap.getOrDefault("职业", ""));
            page.putField("zodiac", fieldsMap.getOrDefault("星座", ""));
            page.putField("bloodType", fieldsMap.getOrDefault("血型", ""));
            page.putField("height", fieldsMap.getOrDefault("身高", ""));
            page.putField("weight", fieldsMap.getOrDefault("体重", ""));
            page.putField("talentagency", fieldsMap.getOrDefault("经纪公司", ""));
            page.putField("school", fieldsMap.getOrDefault("毕业院校", ""));
            page.putField("debut", fieldsMap.getOrDefault("出道年份", ""));
            page.putField("productions", fieldsMap.getOrDefault("代表作品", ""));
            page.putField("describes", describes);
        }

        String currURL = page.getUrl().get();
        int nextID = Integer.parseInt(currURL.substring(currURL.lastIndexOf("-") + 1, currURL.length())) + 1;
        String newURL = currURL.substring(0, currURL.lastIndexOf("-") + 1) + nextID;

        if(page.getStatusCode() == 404) count++; else {count = 0L; sum++;}

        if (count > 500) {
            System.out.println("\n爬取完毕！");
            System.out.println("总共 " + sum + " 个明星");
        } else {page.addTargetRequest(newURL);}
    }

    @Override
    public Site getSite() {return site;}


    public static void main(String[] args) throws Exception {

        String id = "1";
        String outPath = "G:/stars.txt";      //保存路径

        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath(outPath);

        MingXingKuProcessor processor = new MingXingKuProcessor();
//        processor.setEndDate(endDate);

        Spider.create(processor)
                .addUrl("http://www.mingxingku.com/profile-star-" + id)
                .addPipeline(pipeline)
                .thread(5)
                .run();
    }
}
