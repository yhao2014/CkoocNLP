package webmagic.processors;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import utils.ReadDistrict;
import webmagic.pipelines.BasePipeline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 *
 * Created by yhao on 2017/4/6.
 */
public class LianJiaProcessor implements PageProcessor {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.516.400 QQBrowser/9.4.8142.400";
    private Site site = Site.me()
            .setRetryTimes(5)
            .setSleepTime(300)
            .setCycleRetryTimes(6)
//            .setHttpProxyPool(getProxyIP("ckooc-spider/lib/dic/proxyIP"))
//            .setProxyReuseInterval(1000)
            .setUserAgent(UA);

    private static final String URL_LIST = "http://\\w+\\.lianjia\\.com/chengjiao/\\w+/pg\\d+";
    private static final String URL_POST = "http://\\w+\\.lianjia\\.com/chengjiao/\\w+\\d+\\.html";

    private static String district = "";
    private static HashMap<String, String> districtMap = null;
    private static int count = 0;

    public LianJiaProcessor() throws IOException {
    }

    @Override
    public void process(Page page) {
        String post_xpath = "//ul[@class='listContent']/li/a";

        if(page.getUrl().regex(URL_LIST).match()) {
            String url = page.getUrl().get();
            String pageNum = url.substring(url.indexOf("pg") + 2);
            System.out.println("正在抓取第 " + pageNum + " 页...");

            String[] tokens = url.split("chengjiao/");
            if (tokens.length > 1) {
                String districtId = tokens[1].split("/")[0];
                if (!districtId.isEmpty()) district = districtMap.get(districtId);
            }

            //获取详情页
            List<String> l_post = page.getHtml().xpath(post_xpath).links().regex(URL_POST).all();
            if (l_post.isEmpty()) count++; else count = 0;

            if (count > 5) {
                System.out.println("空白页超出最大值，退出！");
                System.exit(0);
            }

            //获取列表页
            String l_list = url.substring(0, url.indexOf("pg") + 2) + (Integer.parseInt(pageNum) + 1);

            page.addTargetRequests(l_post);
            page.addTargetRequest(l_list);
        }else {
            page.putField("title", page.getHtml().xpath("//div[@class='house-title']/div/text()"));       //标题
            page.putField("district", district);        //行政区
            page.putField("totalPrice", page.getHtml().xpath("//span[@class='dealTotalPrice']/i/text()"));      //成交总价
            page.putField("avrgPrice", page.getHtml().xpath("//div[@class='price']/b/text()"));     //成交均价
            List<String> msg = page.getHtml().xpath("//div[@class='msg']/span/label/text()").all();
            if (msg.size() > 5) {
                page.putField("guapaiPrice", msg.get(0));       //挂牌价格(万)
                page.putField("tradeTime", page.getHtml().xpath("//div[@class='house-title']/div/span/text()"));        //交易时间
                page.putField("zhouqi", msg.get(1));        //成交周期(天)
                page.putField("tiaojiaCount", msg.get(2));      //调价(次)
                page.putField("visitCount", msg.get(3));        //带看(次)
                page.putField("attentionCount", msg.get(4));        //关注(人)
                page.putField("scanCount", msg.get(5));     //浏览(次)
            }

            List<String> basicAttr = page.getHtml().xpath("//div[@class='base']/div[@class='content']/ul/li/text()").all();
            if (basicAttr.size() > 13) {
                page.putField("huxing", basicAttr.get(0));      //房屋户型
                page.putField("floor", basicAttr.get(1));       //所在楼层
                page.putField("jianmian", basicAttr.get(2));        //建筑面积
                page.putField("jiegou", basicAttr.get(3));      //户型结构
                page.putField("taonei", basicAttr.get(4));      //套内面积
                page.putField("buildingType", basicAttr.get(5));        //建筑类型
                page.putField("chaoxiang", basicAttr.get(6));       //房屋朝向
                page.putField("buildTime", basicAttr.get(7));       //建成年代
                page.putField("decoration", basicAttr.get(8));      //装修情况
                page.putField("structure", basicAttr.get(9));       //建筑结构
                page.putField("gongnuan", basicAttr.get(10));       //供暖方式
                page.putField("tihu", basicAttr.get(11));       //梯户比例
                page.putField("chanquan", basicAttr.get(12));       //产权年限
                page.putField("elevator", basicAttr.get(13));       //配备电梯
            }

            List<String> tradeAttr = page.getHtml().xpath("//div[@class='transaction']/div[@class='content']/ul/li/text()").all();
            if (tradeAttr.size() > 5) {
                page.putField("lianjiaId", tradeAttr.get(0));       //链家编号
                page.putField("quanshu", tradeAttr.get(1));     //交易权属
                page.putField("guapaiTime", tradeAttr.get(2));      //挂牌时间
                page.putField("yongtu", tradeAttr.get(3));      //房屋用途
                page.putField("nianxian", tradeAttr.get(4));        //房屋年限
                page.putField("fangquan", tradeAttr.get(5));        //房权所属
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) throws IOException {
        String city = "gz";
        String districtId = "tianhe";
        String outPath = "G:/lianjia/"+ city + ".txt";
        BasePipeline pipeline = new BasePipeline();
        pipeline.setPath(outPath);

        ReadDistrict readDistrict = new ReadDistrict();
        districtMap = readDistrict.read("ckooc-spider/lib/dic/districts");

        Spider.create(new LianJiaProcessor())
                .addUrl("http://" + city + ".lianjia.com/chengjiao/" + districtId + "/pg1")
                .addPipeline(pipeline)
                .thread(5)
                .run();
    }
}
