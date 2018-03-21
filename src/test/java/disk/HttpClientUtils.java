package disk;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class HttpClientUtils {
    public static OkHttpClient getOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS) // 链接超时
                .readTimeout(5, TimeUnit.SECONDS) // 读取超时
                .writeTimeout(5, TimeUnit.SECONDS); // 写入超时
    }
}
