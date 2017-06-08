package com.kairui.spider.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class HttpUtils {

    private static final Log log = LogFactory.getLog(HttpUtils.class);

    private static volatile HttpUtils instance;

    private volatile HttpClient client;

    private final BasicCookieStore cookieStore;

    public static String defaultEncoding = "utf-8";

    private static List<NameValuePair> paramsConverter(JSONObject params) {
        Map<String, String> temp = (Map<String, String>) JSON.toJSON(params);
        List<NameValuePair> nvps = new LinkedList<>();
        Set<Entry<String, String>> paramsSet = temp.entrySet();
        nvps.addAll(paramsSet.stream().map(paramEntry -> new BasicNameValuePair(paramEntry.getKey(), paramEntry.getValue())).collect(Collectors.toList()));
        return nvps;
    }

    public static String readStream(InputStream in, String encoding) {
        if (in == null) return null;
        try {
            InputStreamReader inReader;
            if (encoding == null) {
                inReader = new InputStreamReader(in, defaultEncoding);
            } else {
                inReader = new InputStreamReader(in, encoding);
            }
            char[] buffer = new char[1024];
            int readLen;
            StringBuilder sb = new StringBuilder();
            while ((readLen = inReader.read(buffer)) != -1) {
                sb.append(buffer, 0, readLen);
            }
            inReader.close();
            return sb.toString();
        } catch (IOException e) {
            log.error("读取返回内容出错", e);
        }
        return null;
    }

    private HttpUtils() {
        //设置连接参数
        ConnectionConfig connConfig = ConnectionConfig.custom().setCharset(Charset.forName(defaultEncoding)).build();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(100000).build();
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);
        //指定信任密钥存储对象和连接套接字工厂
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            SSLContext sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, new AnyTrustStrategy()).build();
            LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            registryBuilder.register("https", sslSF);
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        //设置连接管理器
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
        connManager.setDefaultConnectionConfig(connConfig);
        connManager.setDefaultSocketConfig(socketConfig);
        //指定cookie存储对象
        cookieStore = new BasicCookieStore();
        //构建客户端
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).setConnectionManager(connManager).build();
    }

    public static HttpUtils getInstance() {
        synchronized (HttpUtils.class) {
            if (HttpUtils.instance == null) {
                instance = new HttpUtils();
            }
            return instance;
        }
    }

    public InputStream doGet(String url) throws URISyntaxException, IOException {
        HttpResponse response = this.doGet(url, null);
        return response != null ? response.getEntity().getContent() : null;
    }

    public String doGetForString(String url) throws URISyntaxException, IOException {
        return HttpUtils.readStream(this.doGet(url), null);
    }

    public InputStream doGetForStream(String url, JSONObject queryParams) throws URISyntaxException, IOException {
        HttpResponse response = this.doGet(url, queryParams);
        return response != null ? response.getEntity().getContent() : null;
    }

    public String doGetForString(String url, JSONObject queryParams) throws URISyntaxException, IOException {
        return HttpUtils.readStream(this.doGetForStream(url, queryParams), null);
    }

    /**
     * 基本的Get请求
     *
     * @param url         请求url
     * @param queryParams 请求头的查询参数
     * @return HttpResponse
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public HttpResponse doGet(String url, JSONObject queryParams) throws URISyntaxException, IOException {
        HttpGet gm = new HttpGet();
        URIBuilder builder = new URIBuilder(url);
        //填入查询参数
        if (queryParams != null && !queryParams.isEmpty()) {
            builder.setParameters(HttpUtils.paramsConverter(queryParams));
        }
        gm.setURI(builder.build());
        return client.execute(gm);
    }

    public InputStream doPostForStream(String url, JSONObject queryParams) throws URISyntaxException, IOException {
        HttpResponse response = this.doPost(url, queryParams, null);
        return response != null ? response.getEntity().getContent() : null;
    }

    public String doPostForString(String url, JSONObject queryParams) throws URISyntaxException, IOException {
        return HttpUtils.readStream(this.doPostForStream(url, queryParams), null);
    }

    public String doPostForString(String url, JSONObject queryParams, JSONObject formParams) throws URISyntaxException, IOException {
        return HttpUtils.readStream(this.doPostForStream(url, queryParams, formParams), null);
    }

    public InputStream doPostForStream(String url, JSONObject queryParams, JSONObject formParams) throws URISyntaxException, IOException {
        HttpResponse response = this.doPost(url, queryParams, formParams);
        return response != null ? response.getEntity().getContent() : null;
    }

    /**
     * 基本的Post请求
     *
     * @param url         请求url
     * @param queryParams 请求头的查询参数
     * @param formParams  post表单的参数
     * @return HttpResponse
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public HttpResponse doPost(String url, JSONObject queryParams, JSONObject formParams) throws URISyntaxException, IOException {
        HttpPost pm = new HttpPost();
        pm.addHeader("Content-Type", "application/json;charset=utf-8");
        if (url.contains("/oauth/token")) pm.addHeader("Authorization", "Basic YnJvd3Nlcjo=");
        URIBuilder builder = new URIBuilder(url);
        //填入查询参数
        if (queryParams != null && !queryParams.isEmpty()) {
            builder.setParameters(HttpUtils.paramsConverter(queryParams));
        }
        pm.setURI(builder.build());
        //填入表单参数
        if (formParams != null && !formParams.isEmpty()) {
            pm.setEntity(new StringEntity(JSON.toJSONString(formParams), defaultEncoding));
        }
        return client.execute(pm);
    }

    /**
     * 基本的Post请求
     *
     * @param url         请求url
     * @param queryParams 请求头的查询参数
     * @param formParams  post表单的参数
     * @return HttpResponse
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClientProtocolException
     */
    public HttpResponse doFormPost(String url, JSONObject queryParams, JSONObject formParams) throws URISyntaxException, IOException {
        HttpPost pm = new HttpPost();
        if (url.contains("/oauth/token")) pm.addHeader("Authorization", "Basic YnJvd3Nlcjo=");
        URIBuilder builder = new URIBuilder(url);
        //填入查询参数
        if (queryParams != null && !queryParams.isEmpty()) {
            builder.setParameters(HttpUtils.paramsConverter(queryParams));
        }
        pm.setURI(builder.build());
        //填入表单参数
        if (formParams != null && !formParams.isEmpty()) {
            pm.setEntity(new UrlEncodedFormEntity(HttpUtils.paramsConverter(formParams), defaultEncoding));
        }
        return client.execute(pm);
    }

    class AnyTrustStrategy implements TrustStrategy {

        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            return true;
        }

    }
}
