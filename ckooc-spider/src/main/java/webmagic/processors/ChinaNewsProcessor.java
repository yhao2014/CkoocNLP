package webmagic.processors;

import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import webmagic.pipelines.BasePipeline;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * "中国新闻网"按类别抓取数据
 * URL: http://www.chinanews.com/scroll-news/[category]/[year]/[day]/news.shtml
 * 中括号部分由自己指定。
 *
 * 爬取字段: 类别ID、类别中文名、标题、日期、时间、编辑、关键词、摘要、内容
 * <p/>Created by Administrator on 2016/7/4.
 */
public class ChinaNewsProcessor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me()
            .setAcceptStatCode(new HashSet<Integer>(){{add(200); add(404);}})
            .setCharset("gbk")
            .setRetryTimes(5)
            .setSleepTime(300)
            .setCycleRetryTimes(6)
            .setUserAgent(UA);

    private static final String URL_LIST = "http://\\w+\\.chinanews\\.com/scroll-news/\\w+/\\d+/\\d+/news.shtml";
    private static final String URL_POST = "http://\\w+\\.chinanews\\.com/\\w+/\\d+/\\d+\\-\\d+/\\d+\\.shtml";

    private static Long count = 0L;
    private static Long sum = 0L;

    private String endDate = "0";

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * 将URL中包含的日期增加指定的天数
     *
     * @param url  输入的URL
     * @param days 增加的天数
     * @return 返回新URL
     */
    private static String addDay(String url, int days) throws Exception {
        String[] fields = url.split("/");
        String year = fields[5];
        String day = fields[6];

        String startDay = year + day;

        Date d1 = new SimpleDateFormat("yyyyMMdd").parse(startDay);

        Calendar cal = Calendar.getInstance();
        cal.setTime(d1);

        cal.add(Calendar.DAY_OF_MONTH, days);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(cal.getTime());

        System.out.println("\n开始处理：" + year + day);

        String newURL = "";
        for (int i = 0; i < fields.length - 3; i++) {
            newURL += fields[i] + "/";
        }
        newURL += date.substring(0, 4) + "/" + date.substring(4, date.length()) + "/" + fields[fields.length - 1];
        return newURL;
    }


    @Override
    public void process(us.codecraft.webmagic.Page page) {
        String post_xpath = "//div[@class='content_list']/ul/li";

        //处理列表页，将其加入待抓取队列
        if (page.getUrl().regex(URL_LIST).match()) {

            String[] fields = page.getUrl().get().split("/");
            String dateStr = fields[5] + fields[6];
            int date = Integer.parseInt(dateStr);

            if (date >= Integer.parseInt(endDate) && count < 500) {
                if (page.getStatusCode() != 404) {
                    List<String> l_post = page.getHtml().xpath(post_xpath).links().regex(URL_POST).all();
                    page.addTargetRequests(l_post);
                    count = 0L;
                }

                try {
                    String newURL = addDay(page.getUrl().get(), -1);
                    page.addTargetRequest(newURL);
                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("\n爬取完毕！最后页面日期：" + getEndDate());
                System.out.println("总共 " + sum + " 个页面");
            }
        } else {
            sum++;

            //处理详情页,添加需要爬取的字段
            page.putField("categoryId", page.getHtml().xpath("//input[@id='newstype']/@value"));      //类别(英文)
            page.putField("category", page.getHtml().xpath("//div[@id='nav']/a/text()").all());     //类别(中文)
            page.putField("title", page.getHtml().xpath("//input[@id='newstitle']/@value"));        //标题
            page.putField("date", page.getHtml().xpath("//input[@id='newsdate']/@value"));      //日期
            page.putField("time", page.getHtml().xpath("//input[@id='newstime']/@value"));      //时间
            page.putField("editor", page.getHtml().xpath("//input[@id='editorname']/@value"));      //编辑
            page.putField("keywords", page.getHtml().xpath("//meta[@name='keywords']/@content"));       //关键词
            page.putField("description", page.getHtml().xpath("//meta[@name='description']/@content"));     //摘要
            page.putField("content", page.getHtml().xpath("//div[@class='left_zw']/p/text()").all());       //内容
        }
    }

    @Override
    public Site getSite() {
        return site;
    }


    public static void main(String[] args) throws Exception {
        String year = "2015";
        String startDate = "0511";
//        String endDate = "20170315";

        String category = "gn";     //待抓取的类别（可通过url查看）
        String outPath = "G:/chinaNews/" + category + ".txt";      //保存路径

        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath(outPath);

        ChinaNewsProcessor processor = new ChinaNewsProcessor();
//        processor.setEndDate(endDate);

        Spider.create(processor)
                .addUrl("http://www.chinanews.com/scroll-news/" + category + "/" + year + "/" + startDate + "/news.shtml")
                .addPipeline(pipeline)
                .thread(5)
                .run();
    }
}
