package disk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class WeChatApiService {
    // 国内WEB微信地址
    /**
     * 国内:http://login.wx.qq.com
     * 国际:http://login.web.wechat.com
     */
    public static final String WECHAT_API_URL = "http://login.wx.qq.com";
    public String wxRootURL;// wx2.qq.com or wx.qq.com
    public String wxURL;//

    public static final String WECHAT_APPID = "wx782c26e4c19acffb";
    public static final String WECHAT_LANG = "zh_CN";

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

    private Request.Builder builder;

    public WeChatApiService() {
        builder = new Request.Builder().addHeader("user-agent", USER_AGENT);
    }

    public String getUUID() throws Exception {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(WECHAT_API_URL + "/jslogin").newBuilder();
        urlBuilder.addQueryParameter("appid", WECHAT_APPID);
        urlBuilder.addQueryParameter("fun", "new");
        urlBuilder.addQueryParameter("lang", WECHAT_LANG);
        urlBuilder.addQueryParameter("_", String.valueOf(System.currentTimeMillis()));

        Request request = builder.url(urlBuilder.build()).build();

        final Call call = HttpClientUtils.getOkHttpClient().newCall(request);
        // 执行请求
        final Response response = call.execute();

        String rsp = response.body().string();
        String uuid = rsp.substring(rsp.indexOf('"') + 1, rsp.lastIndexOf('"'));
        return uuid;
    }

    public byte[] getQRCode(String uuid) throws Exception {
        Request request = builder.url(WECHAT_API_URL + "/qrcode/" + uuid).build();

        final Call call = HttpClientUtils.getOkHttpClient().newCall(request);
        // 执行请求
        final Response response = call.execute();

        return response.body().bytes();
    }

    public String login(String uuid) throws Exception {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(WECHAT_API_URL + "/cgi-bin/mmwebwx-bin/login").newBuilder();
        urlBuilder.addQueryParameter("loginicon", "true");
        urlBuilder.addQueryParameter("uuid", uuid);
        urlBuilder.addQueryParameter("tip", "0");
        urlBuilder.addQueryParameter("r", "-226676679");
        urlBuilder.addQueryParameter("_", String.valueOf(System.currentTimeMillis()));

        Request request = builder.url(urlBuilder.build()).build();

        final Call call = HttpClientUtils.getOkHttpClient().newCall(request);
        // 执行请求
        final Response response = call.execute();

        String rsp = response.body().string();

        if (rsp.indexOf("window.code=200") != -1) {
            String[] content = rsp.split("\n");
            String regex = "window.redirect_uri=\"(\\S+)\";";
            String redirectUri = "";
            Matcher matcher = Pattern.compile(regex).matcher(content[1]);
            if (matcher.find()) {
                redirectUri = matcher.group(1);
            }
            String tmpUrl = redirectUri.replaceAll("https://", "");
            wxRootURL = tmpUrl.substring(0, tmpUrl.indexOf("/")).trim();
            wxURL = redirectUri.substring(0, redirectUri.lastIndexOf("/"));

            Request lrequest = builder.url(redirectUri).build();

            final Call lcall = HttpClientUtils.getOkHttpClientBuilder().followRedirects(false).followSslRedirects(false)
                    .build().newCall(request);
            // 执行请求
            final Response lresponse = call.execute();
            String string = lresponse.body().string();
            return string;
        }
        throw new IllegalArgumentException(rsp);
    }
}
