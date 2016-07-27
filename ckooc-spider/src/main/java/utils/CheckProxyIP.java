package utils;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yhao on 2016/7/21.
 */
public class CheckProxyIP {

    public static String convertStreamToString(InputStream is) {
        if (is == null)
            return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("/n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();

    }


    public static String checkIPAddress(String ip, int port, String reqUrl) {
        URL url = null;

        String availableIp = null;
        try {
            url = new URL(reqUrl);
        } catch (MalformedURLException e) {
            System.out.println("url invalidate");
        }
        InetSocketAddress addr;
        addr = new InetSocketAddress(ip, port);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr); // http proxy
        InputStream in = null;
        try {
            assert url != null;
            URLConnection conn = url.openConnection(proxy);
            conn.setConnectTimeout(1000);
            in = conn.getInputStream();
        } catch (Exception e) {
//            System.out.println("ip " + ip + " is not aviable");//异常IP
        }

        String s = convertStreamToString(in);
        System.out.println(s);
        // System.out.println(s);
        if (s.indexOf("baidu") > 0) {//有效IP
            System.out.println(ip + ":" + port + " is ok");
            availableIp = ip + ":" + port;
        }

        return availableIp;
    }


    public static HashMap<String, Integer> checkIPAddress(HashMap<String, Integer> proxyIpMap, String reqUrl) {
        HashMap<String, Integer> availableIpMap = new HashMap<>();
        for (String proxyHost : proxyIpMap.keySet()) {
            Integer proxyPort = proxyIpMap.get(proxyHost);
            String availableIp = checkIPAddress(proxyHost, proxyPort, reqUrl);
            if (availableIp != null) {
                availableIpMap.put(proxyHost, proxyPort);
            }
        }

        return availableIpMap;
    }


    /**
     * 代理IP有效检测
     *
     * @param proxyIp
     * @param proxyPort
     * @param reqUrl
     */
    public static void checkProxyIp(String proxyIp, int proxyPort, String reqUrl) {
        Map<String, Integer> proxyIpMap = new HashMap<String, Integer>();
        proxyIpMap.put(proxyIp, proxyPort);
        checkProxyIp(proxyIpMap, reqUrl);
    }

    /**
     * 批量代理IP有效检测
     *
     * @param proxyIpMap
     * @param reqUrl
     */
    public static void checkProxyIp(Map<String, Integer> proxyIpMap, String reqUrl) {

        for (String proxyHost : proxyIpMap.keySet()) {
            Integer proxyPort = proxyIpMap.get(proxyHost);

            int statusCode = 0;

            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            CloseableHttpClient httpClient = httpClientBuilder.build();

            // 连接超时时间（默认10秒 10000ms） 单位毫秒（ms）
            int connectionTimeout = 5000;
            // 读取数据超时时间（默认30秒 30000ms） 单位毫秒（ms）
            int soTimeout = 10000;

            HttpHost proxy = new HttpHost(proxyHost, proxyPort);

            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .setConnectTimeout(connectionTimeout)
                    .setSocketTimeout(soTimeout)
                    .build();

            HttpPost httpPost = new HttpPost(reqUrl);
            HttpHost target = new HttpHost(reqUrl);

            httpPost.setConfig(config);

            try {
                CloseableHttpResponse response = httpClient.execute(target, httpPost);
                statusCode = response.getStatusLine().getStatusCode();
            } catch (IOException e) {
                System.out.format("%s:%s 连接超时！\n", proxyHost, proxyPort);
                continue;
            }

            System.out.format("%s:%s-->%s\n", proxyHost, proxyPort, statusCode);
        }
    }

    public static void main(String[] args) throws Exception {
        String targetURL = "http://www.baidu.com";

        String path = "ckooc-spider/lib/dic/proxyIP";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String line = br.readLine();

        HashMap<String, Integer> ipPool = new HashMap<>();
        while (line != null) {
            String[] tokens = line.split(":");
            ipPool.put(tokens[0], Integer.parseInt(tokens[1]));
            line = br.readLine();
        }

        HashMap<String, Integer> availableIpMap = checkIPAddress(ipPool, targetURL);
        for (String availableHost : availableIpMap.keySet()) {
            Integer availablePort = availableIpMap.get(availableHost);

            System.out.println(availableHost + ":" + availablePort);
        }

//        checkProxyIp(ipPool, targetURL);
    }
}
