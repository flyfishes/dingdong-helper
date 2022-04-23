import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Application {

    public static class TimeGap {
        public int nType; // <0,=0,>0
        public long begintime;
        public long endtime;

        public TimeGap(int nType, long begintime, long endtime) {
            super();
            this.nType = nType;
            this.begintime = begintime;
            this.endtime = endtime;
        }
    }

    /*
     * map_config
     * String 闲时idle_n、忙时busy_n
     * <Long,Long> begin、end time
     */
    private static List<TimeGap> map_config = new ArrayList<TimeGap>();

    public static final Map<String, Map<String, Object>> map = new ConcurrentHashMap<>();

    public static final Logger logger = LogManager.getLogger(Application.class);  
    /**
     * 获取当前时间状态（忙时秒抢，闲时间隔抢）
     * 
     * @param bRefreshConfig 是否刷新配置文件
     * @return <0:忙；=0:单次；>0(闲时，间隔时间：秒)
     */
    public static int getCurrentState(boolean bRefreshConfig) {       
        if (bRefreshConfig)
            LoadProperties();
        try {
            // 求出现在的秒
            long totalSeconds = System.currentTimeMillis() / 1000;
            long totalMinutes = totalSeconds / 60;
            long totalHour = totalMinutes / 60 + 8;
            long dateNow = totalSeconds % 60 + totalMinutes % 60 * 100 + totalHour % 24 * 10000;
            // System.out.println(dateNow);
            // System.out.println(System.currentTimeMillis());
            logger.debug("getCurrentState()");

            int nLen = map_config.size();
            for (int i = 0; i < nLen; i++) {
                if (dateNow >= map_config.get(i).begintime && dateNow < map_config.get(i).endtime)
                    return map_config.get(i).nType;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    public static void sleep(int nSleeptime) {
        try {
            Thread.sleep(nSleeptime + System.currentTimeMillis() % 16);
        } catch (InterruptedException ignored) {
        }
    }

    public static void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 加载参数配置文件
     */
    public static void LoadProperties() {
        try {
            FileInputStream fi = new FileInputStream("dingdong.properties");
            Properties pro = new Properties();
            pro.load(fi);
            map_config.clear();
            String str1, str2;
            Integer idleGap=Integer.parseInt(pro.getProperty("idleGap" ,"60000"));
            for (int i = 0; i < 10; i++) {
                str1 = pro.getProperty("idlebegin" + i);// HHmmss
                str2 = pro.getProperty("idleend" + i);
                if (str1 != null && str2 != null) {
                    try {
                        TimeGap tg = new TimeGap(idleGap+(i>6?1000*3600:i), Long.parseLong(str1), Long.parseLong(str2));
                        map_config.add(tg);
                    } catch (NumberFormatException ignored) {
                    }
                }
                str1 = pro.getProperty("busybegin" + i);
                str2 = pro.getProperty("busyend" + i);
                if (str1 != null && str2 != null) {
                    try {
                        TimeGap tg = new TimeGap(-1, Long.parseLong(str1), Long.parseLong(str2));
                        map_config.add(tg);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            fi.close();
        } catch (Exception e) {
        }
        ;
    }

    public static void main(String[] args) {
        int iTimegap = 0;
        logger.trace("trace1");
        logger.debug("debug2");
        logger.info("info3");
        logger.warn("warning4");
        logger.error("error5");
        logger.fatal("fatal6");

        if (UserConfig.addressId.length() == 0) {
            logger.error("请先执行UserConfig获取配送地址id");
            return;
        }
        int loop = 0;
        do {
            long totalSeconds = System.currentTimeMillis() / 1000;
            //long totalMinutes = totalSeconds / 60;
            //long totalHour = totalMinutes / 60 + 8;
            //long dateNow = totalSeconds % 60 + totalMinutes % 60 * 100 + totalHour % 24 * 10000;
            //System.out.print(Long.toString(dateNow)+" ,loop=");
            //System.out.println(loop++);

            iTimegap = getCurrentState(true);
            logger.info("loop="+(loop++) +",gap="+iTimegap);
            // 此为单次执行模式 用于在非高峰期测试下单 也必须满足3个前提条件 1.有收货地址 2.购物车有商品 3.能选择配送信息
            while (iTimegap >= 0) {
                Map<String, Object> cartMap = Api.getCart();
                if (cartMap != null) {
                    Map<String, Object> multiReserveTimeMap = Api.getMultiReserveTime(UserConfig.addressId, cartMap);
                    if (multiReserveTimeMap != null) {
                        Map<String, Object> checkOrderMap = Api.getCheckOrder(UserConfig.addressId, cartMap,
                                multiReserveTimeMap);
                        if (checkOrderMap != null) {
                            // for (int jj=0;jj<10;jj++){
                            Api.addNewOrder(UserConfig.addressId, cartMap, multiReserveTimeMap, checkOrderMap);
                            // }
                        }

                    }
                }
                iTimegap = getCurrentState(true);
                if (iTimegap == 0)
                    return;
                else if (iTimegap > 0){
                    logger.trace("continue idle loop");
                    sleep(iTimegap);
                }else
                    break;
            }

            // 此为高峰期策略 通过同时获取或更新 购物车、配送、订单确认信息再进行高并发提交订单
            for (int i = 0; i < 3; i++) {
                new Thread(() -> {
                    while (!map.containsKey("end")) {
                        sleep();
                        Map<String, Object> cartMap = Api.getCart();
                        if (cartMap != null) {
                            map.put("cartMap", cartMap);
                        }
                        if (getCurrentState(false) >= 0) {
                            map.put("end", new HashMap<>());
                        }
                    }
                }).start();
            }
            for (int i = 0; i < 3; i++) {
                new Thread(() -> {
                    while (!map.containsKey("end")) {
                        sleep();
                        if (map.get("cartMap") == null) {
                            continue;
                        }
                        Map<String, Object> multiReserveTimeMap = Api.getMultiReserveTime(UserConfig.addressId,
                                map.get("cartMap"));
                        if (multiReserveTimeMap != null) {
                            map.put("multiReserveTimeMap", multiReserveTimeMap);
                        }
                        if (getCurrentState(false) >= 0) {
                            map.put("end", new HashMap<>());
                        }
                    }
                }).start();
            }
            for (int i = 0; i < 3; i++) {
                new Thread(() -> {
                    while (!map.containsKey("end")) {
                        sleep();
                        if (map.get("cartMap") == null || map.get("multiReserveTimeMap") == null) {
                            continue;
                        }
                        Map<String, Object> checkOrderMap = Api.getCheckOrder(UserConfig.addressId, map.get("cartMap"),
                                map.get("multiReserveTimeMap"));
                        if (checkOrderMap != null) {
                            map.put("checkOrderMap", checkOrderMap);
                        }
                        if (getCurrentState(false) >= 0) {
                            map.put("end", new HashMap<>());
                        }
                    }
                }).start();
            }
            for (int i = 0; i < 12; i++) {
                new Thread(() -> {
                    while (!map.containsKey("end")) {
                        if (map.get("cartMap") == null || map.get("multiReserveTimeMap") == null
                                || map.get("checkOrderMap") == null) {
                            sleep();
                            continue;
                        }
                        Api.addNewOrder(UserConfig.addressId, map.get("cartMap"), map.get("multiReserveTimeMap"),
                                map.get("checkOrderMap"));
                        if (getCurrentState(false) >= 0) {
                            map.put("end", new HashMap<>());
                        }
                    }
                }).start();
            }

            while (!map.containsKey("end")) {
                sleep(5000);
            }
            map.clear();
        } while (iTimegap != 0);
    }
}
