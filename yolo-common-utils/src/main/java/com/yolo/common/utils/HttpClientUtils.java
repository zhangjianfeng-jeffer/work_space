package com.yolo.common.utils;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtils {

    public static CloseableHttpClient httpClient;
    private Object lock = new Object();
    private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    private HttpClientUtils() {
        init();
    }

    private void init() {
        if (httpClient == null) {
            synchronized (lock) {
                if (httpClient == null) {
                    try {
                        // 创建httpclient连接池
                        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
                        httpClientConnectionManager.setMaxTotal(300); // 设置连接池线程最大数量
                        httpClientConnectionManager.setDefaultMaxPerRoute(300); // 设置单个路由最大的连接线程数量
                        // 创建http request的配置信息
                        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(30000).setSocketTimeout(30000).build();
                        // 设置重定向策略
                        LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();
                        // 初始化httpclient客户端
                        httpClient = HttpClients.custom().setConnectionManager(httpClientConnectionManager).setDefaultRequestConfig(requestConfig).setRedirectStrategy(redirectStrategy).build();

                    }
                    catch (Exception e) {
                    	logger.error("", e);
                    }

                }
            }
        }

    }

    public static String postOrGet(String url, Map<String, String> params, boolean isPost) throws Exception{
        if (url == null || url.trim().equals("")) {
            throw new NullPointerException("URL不能为空");
        }
        if (httpClient == null) {
            new HttpClientUtils();
        }
        if (httpClient == null) {
            throw new Exception("HttpClient初始化失败");
        }
        CloseableHttpResponse response = null;
        try {
            if(isPost){
                HttpPost post = new HttpPost(url);
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                if (params != null && params.size() > 0) {
                    Set<String> keys = params.keySet();
                    for (String string : keys) {
                        nvps.add(new BasicNameValuePair(string, params.get(string)));
                    }
                }
                post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                response = httpClient.execute(post);
            }else{
                StringBuffer buffer = new StringBuffer();
                if(params != null && params.size() > 0){
                    Set<String> keys = params.keySet();
                    for (String key : keys) {
                        buffer.append(key).append("=").append(params.get(key)).append("&");
                    }
                }
                if(buffer.length() > 0){
                    buffer.setLength(buffer.length() - 1);
                    url +="?" + buffer.toString();
                }
                HttpGet get = new HttpGet(url);
                logger.info(url);
                response = httpClient.execute(get);
            }
            
            logger.info("request URL:{},response status:{}", url,response.getStatusLine());
            return EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            try {
                if(response != null){
                    response.close();
                }
            }
            catch (Exception e) {}

        }

    }

    public static int execute(String uri, Map<String, Object> params, boolean isPost)
            throws Exception {
        if (uri == null || uri.trim().equals("")) {
            return -1;
        }
        if (httpClient == null) {
            new HttpClientUtils();
        }
        if (httpClient == null) {
            return -1;
        }
        CloseableHttpResponse response = null;
        try {
            if (isPost) {
                HttpPost post = new HttpPost(uri);
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                if (params != null && params.size() > 0) {
                    Set<String> keys = params.keySet();
                    for (String string : keys) {
                        nvps.add(new BasicNameValuePair(string, (String) params.get(string)));
                    }

                }
                post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                response = httpClient.execute(post);
            } else {
                HttpGet get = new HttpGet(uri);
                response = httpClient.execute(get);
            }

            // HttpEntity httpEntity = response.getEntity();
            System.out.println("response status: " + response.getStatusLine());
            // String body = EntityUtils.toString(httpEntity,
            // Charset.forName("utf-8"));
            // System.out.println(String.format("URI{%s},result{%s}", uri,
            // body));
            return response.getStatusLine().getStatusCode();
        }
        finally {
            if (response != null) {
                response.close();
            }
        }

    }
}

