package disk;

public class Main {
    public static void main(String[] args) {
        WeChatControl weChatControl = new WeChatControl();
        try {
            weChatControl.login();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
