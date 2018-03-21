package disk;

import java.io.File;

import javax.imageio.stream.FileImageOutputStream;

public class WeChatControl {
    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }
    private String uuid;
    public WeChatApiService weChatApiService;
    public ThreadPool threadPool;

    //
    public WeChatControl() {
        weChatApiService = new WeChatApiService();
        threadPool = new ThreadPool();
        threadPool.start();
    }

    public void login() throws Exception {
        try {
            uuid = weChatApiService.getUUID();
            System.out.println(uuid);
            byte[] qrcode = weChatApiService.getQRCode(uuid);
            File file = File.createTempFile("qrcode" + System.currentTimeMillis(), ".jpg");
            FileImageOutputStream baos = new FileImageOutputStream(file);
            baos.write(qrcode, 0, qrcode.length);
            baos.close();
            System.out.println(file.getCanonicalPath());
            threadPool.executeThreadWorker(this::startLoginThread);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLoginThread() {
        String loginRsp;
        System.out.println(">>>1.login");
        // check login
        while (true) {
            System.out.println(System.currentTimeMillis());
            try {
                loginRsp = weChatApiService.login(uuid);
                break;
            } catch (Exception e) {
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(loginRsp);
    }
}
