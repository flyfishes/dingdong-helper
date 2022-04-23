import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息
 * 通过小程序抓包购物车接口获取headers和body中的数据填入
 */
public class UserConfig {

    //收货地址id
    public static final String addressId = "5e4347c52956c67a61d3aa21";

    /**
     * 提前获取收货地址id 填写到addressId上 规则为该站点可送达的默认收货地址  如果没有请自行去APP中设置好再运行
     * 每天抢之前先允许一下此接口 确认登录信息是否有效 如果失效了重新抓一次包
     */
    public static void main(String[] args) {
        String addressId = Api.getAddressId();
        System.out.println("addressId：" + addressId);
    }

    /**
     * 抓包后参考项目中的image/headers.jpeg 把信息一行一行copy到下面 没有的key不需要复制
     */
    public static Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("ddmc-city-number", "0101");
        headers.put("ddmc-build-version", "2.82.0");
        headers.put("ddmc-device-id", "osP8I0cgv5aemgvIES8YbaBdeRTE");
        headers.put("ddmc-station-id", "5dc2a0f626c3d171698b456c");
        headers.put("ddmc-channel", "applet");
        headers.put("ddmc-os-version", "[object Undefined]");
        headers.put("ddmc-app-client-id", "4");
        headers.put("cookie", "DDXQSESSID=d75bfba6b810024ecbe5f8ec6c0c8e26");
        headers.put("ddmc-ip", "");
        headers.put("ddmc-longitude", "31.151237");
        headers.put("ddmc-latitude", "121.398265");
        headers.put("ddmc-api-version", "9.49.2");
        headers.put("ddmc-uid", "5e3d8d0d0e707349008e2004");
        headers.put("user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 15_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.18(0x1800123c) NetType/WIFI Language/zh_CN");
        headers.put("referer", "https://servicewechat.com/wx1e113254eda17715/422/page-frame.html");
        return headers;
    }

    /**
     * 抓包后参考项目中的image/body.jpeg 把信息一行一行copy到下面 没有的key不需要复制
     *
     * 这里不能加泛型 有些接口是params  泛型必须要求<String,String> 有些是form表单 泛型要求<String,Object> 无法统一
     */
    public static Map getBody() {
        Map body = new HashMap<>();
        body.put("uid", "5e3d8d0d0e707349008e2004");
        body.put("longitude", "121.398265");
        body.put("latitude", "31.151237");
        body.put("station_id", "5dc2a0f626c3d171698b456c");
        body.put("city_number", "0101");
        body.put("api_version", "9.49.2");
        body.put("app_version", "2.82.0");
        body.put("applet_source", "");
        body.put("channel", "applet");
        body.put("app_client_id", "4");
        body.put("sharer_uid", "");
        body.put("openid", "osP8I0cgv5aemgvIES8YbaBdeRTE");
        body.put("h5_source", "");
        body.put("device_token", "WHJMrwNw1k/FKPjcOOgRd+A/u6bx57E4BvpTzMDyjmXuma0SjlQstPnhXTUuX7qrr3UvaJH5cjKDmZsx873ZLYf9+OpmctDyXdCW1tldyDzmauSxIJm5Txg==1487582755342");
        return body;
    }

}
