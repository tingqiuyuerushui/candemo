package com.uaes.candemo;

import com.uaes.candemo.config.Code;
import com.uaes.candemo.file.CanReciveFile;
import com.uaes.candemo.file.WriteFile;
import com.uaes.candemo.util.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@ComponentScan(basePackages = "com.uaes.candemo")
public class CandemoApplication {
    private static RandomAccessFile canReader;
    private static final String CANMSG_PATH = "C:\\test\\rxfile.dat";
    //	private static final String CANMSG_PATH = "D:\\SW\\test2line.dat";
    private static Thread canThread;
    private static Thread pushThread;
    private static String pubTopic = "";
    private static String subTopic = "";
    private static String productKey = "zzblOv3BeNg";
    private static String deviceName = "device2";
    private static String deviceSecret = "esbuyafg4pVHThSV57TwYMM200BnCrMM";
    private static int seekPosition = 0;
    //生成文件路径
    private static String path = "C:\\test\\";

    //文件路径+名称
    private static String filenameTempB3;
    private static String filenameTemp60;
    private static String filenameTemp78;
    private static String filenameB3 = "txfileB3.dat";
    private static String filename60 = "txfile60.dat";
    private static String filename78 = "txfile78.dat";
    private final static String CANID60 = "60";
    private final static String CANIDB3 = "b3";
    private final static String CANID78 = "78";
    private static WriteFile file60;
    private static WriteFile fileB3;
    private static WriteFile file78;
    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(CandemoApplication.class, args);
        QueueUtils.init();
        pubTopic = "/" + productKey + "/" + deviceName + "/update";
        subTopic = "/" + productKey + "/" + deviceName + "/get";
        filenameTempB3 = path+filenameB3;
        filenameTemp60 = path+filename60;
        filenameTemp78 = path+filename78;
//		Thread.sleep(15000);
        if (canThread == null) {
            canThread = new CanThread();
        }
        if (!canThread.isAlive()) {
            canThread.start();
        }
        if (pushThread == null) {
            pushThread = new PushThread();
        }
        if (!pushThread.isAlive()) {
            pushThread.start();
        }
        //init file B3 and 60
        file60 = new CanReciveFile(filenameTemp60);
        fileB3 = new CanReciveFile(filenameTempB3);
        file78 = new CanReciveFile(filenameTemp78);
//		new ForFile(filenameTempB3,filenameTemp60,filenameTemp78);
    }
    /*
     * 从Can上读取数据
     */
    static class CanThread extends Thread {
        @Override
        public void run() {
            try {
                canReader = new RandomAccessFile(CANMSG_PATH, "r");
                canReader.length();
                byte[] buffer = new byte[16];
                ByteBuffer byteBuffer = ByteBuffer.allocate(20);

                while (true) {
                    if (canReader != null) {
                        try {
                            canReader.seek(seekPosition); // 把指针指到头
                            int count = canReader.read(buffer); // 读1帧
                            if (count > 0) {
                                seekPosition = seekPosition+16;
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < count; i++) {
                                    sb.append(String.format("%02X", buffer[i]));
                                }
//								int time = byteArrayToInt(buffer);
////								System.out.println("time: " + time);
                                int canId = byteArrayToInt(new byte[]{buffer[4], buffer[5], buffer[6], buffer[7]});
//                                LogUtil.print("canId: " + String.format("%02X", canId));

//                                if ((canId == 0xFA && isFastByPeriod(1000)) || (canId == 0xFB && isFastByPeriod(1000))) {
//                                    continue;
//                                }
                                if(isFastByPeriod(1000,canId)){
                                    continue;
                                }
//                                LogUtil.print(System.currentTimeMillis() + ": put to queue");

                                byteBuffer.clear();
                                byteBuffer.putLong(System.currentTimeMillis()); // 放入时间
//								System.out.println("time: " + System.currentTimeMillis());
                                byteBuffer.put(buffer, 4, 12); // 把读到的帧放进来丢弃前四个字节
                                QueueUtils.add(byteBuffer.array());
                            } else {
                                Thread.sleep(10);
                            }
                        } catch (Exception e) {
                            System.out.println("发生错误: " + e.getLocalizedMessage());
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class PushThread extends Thread {

        @Override
        public void run() {

            // 第二步：上传消息
            while (true) {
                try {
                    //客户端设备自己的一个标记，建议是MAC或SN，不能为空，32字符内
                    String clientId = InetAddress.getLocalHost().getHostAddress();

                    //设备认证
                    Map<String, String> params = new HashMap<>();
                    System.out.println("开始连接，deviceName = " + deviceName + ", deviceSecret = " + deviceSecret);
                    params.put("productKey", productKey); //这个是对应用户在控制台注册的 设备productkey
                    params.put("deviceName", deviceName); //这个是对应用户在控制台注册的 设备name
                    params.put("clientId", clientId);
                    String t = System.currentTimeMillis() + "";
                    params.put("timestamp", t);

                    //MQTT服务器地址，TLS连接使用ssl开头
                    String targetServer = "ssl://" + productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";

                    //客户端ID格式，两个||之间的内容为设备端自定义的标记，字符范围[0-9][a-z][A-Z]
                    String mqttclientId = clientId + "|securemode=2,signmethod=hmacsha1,timestamp=" + t + "|";
                    String mqttUsername = deviceName + "&" + productKey; //mqtt用户名格式
                    String mqttPassword = SignUtil.sign(params, deviceSecret, "hmacsha1"); //签名

                    System.err.println("mqttclientId=" + mqttclientId);

                    connectMqtt(targetServer, mqttclientId, mqttUsername, mqttPassword, deviceName);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("发生错误，10s后重连...");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        private void connectMqtt(String url, String clientId, String mqttUsername,
                                 String mqttPassword, final String deviceName) throws Exception {
            MemoryPersistence persistence = new MemoryPersistence();
            SSLSocketFactory socketFactory = createSSLSocket();
            final MqttClient sampleClient = new MqttClient(url, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setMqttVersion(4); // MQTT 3.1.1
            connOpts.setSocketFactory(socketFactory);

            //设置是否自动重连
            connOpts.setAutomaticReconnect(true);

            //如果是true，那么清理所有离线消息，即QoS1或者2的所有未接收内容
            connOpts.setCleanSession(false);

            connOpts.setUserName(mqttUsername);
            connOpts.setPassword(mqttPassword.toCharArray());

            System.out.println(clientId + "进行连接,目的地: " + url);
            sampleClient.connect(connOpts);
//            sampleClient.setTimeToWait(60 * 1000); // 设置60s的Timeout

            sampleClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("连接失败,原因:" + cause);
                    cause.printStackTrace();
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("接收到消息,来至Topic [" + topic + "] , 内容是:["
                            + new String(message.getPayload(), "UTF-8") + "],  ");
                    System.out.println(message.getPayload());
                    String CanID = byte2hex(message.getPayload()).substring(8,10);
                    System.out.println("CanID="+CanID);
                    if(CanID.equals(CANID60)){
                        file60.writeFile(message.getPayload());
//						ForFile.writeTo60File(message.getPayload());
                    }else if(CanID.equals(CANIDB3)){
                        fileB3.writeFile(message.getPayload());
//						ForFile.writeToB3File(message.getPayload());
                    }else if(CanID.equals(CANID78)){
                        file78.writeFile(message.getPayload());
//						ForFile.writeTo78File(message.getPayload());
                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //如果是QoS0的消息，token.resp是没有回复的
                    System.out.println("消息发送成功! " + ((token == null || token.getResponse() == null) ? "null"
                            : token.getResponse().getKey()));
                }
            });
            System.out.println("连接成功:---");

            while (true) {
                if (!sampleClient.isConnected()) {
                    System.out.println("已断开连接，请求重连");
                }

                if (QueueUtils.canTake()) {

                    byte[] rawData = QueueUtils.take();
                    byte[] gzipData = CompressUtils.compress(rawData);

                    MqttMessage message = new MqttMessage(gzipData);

                    StringBuilder sb = new StringBuilder();
                    sb.append("Raw data: ");
                    for (byte aRawData : rawData) {
                        sb.append(String.format("%02X ", aRawData));
                    }
                    System.out.println(sb.toString());

//					StringBuilder sb2 = new StringBuilder();
//					sb2.append("Gzip data : ")
//							.append((float)gzipData.length / rawData.length * 100)
//							.append("%");
//                    for (byte aGzipData : gzipData) {
//                        sb2.append(String.format("%02X ", aGzipData));
//                    }
//					System.out.println(sb2.toString());

                    message.setQos(0);
                    System.out.println("消息发布:---");
                    sampleClient.publish(pubTopic, message);
                    //订阅消息
                    sampleClient.subscribe(subTopic);
                    // 100ms一次
//					Thread.sleep(150);
                } else {
                    // 1s一次
                    Thread.sleep(1000);
                }
            }

        }
    }

    public static String byte2hex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String tmp = "";
        for (int i = 0; i < b.length; i++) {
            tmp = Integer.toHexString(b[i] & 0XFF);
            if (tmp.length() == 1){ sb.append("0" + tmp);

            }else{
                sb.append(tmp);
            }
        }
        return sb.toString();
    }
    private static SSLSocketFactory createSSLSocket() throws Exception {

        SSLContext context = SSLContext.getInstance("TLSV1.2");
        context.init(null, new TrustManager[]{new ALiyunIotX509TrustManager()}, null);
        SSLSocketFactory socketFactory = context.getSocketFactory();
        return socketFactory;
    }
    public static int byteArrayToInt(byte[] b) {
        return  (b[3] & 0xFF) << 24 |
                (b[2] & 0xFF) << 16 |
                (b[1] & 0xFF) << 8 |
                (b[0] & 0xFF);
    }
    //    private static long lastTime;
//    public static boolean isFastByPeriod(long IntervalTime,int CANID) {
//        long CurrentTime = System.currentTimeMillis();
//        if ( CurrentTime - lastTime < IntervalTime) {
//            return true;
//        }
//        lastTime = CurrentTime;
//        return false;
//    }
    public static boolean isFastByPeriod(long IntervalTime,int CANID) {
        long CurrentTime = System.currentTimeMillis();
        switch (CANID){
            case Code.ID410:
                if ( CurrentTime - Code.lastTimeID410 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID410 = CurrentTime;
                return false;
            case Code.ID411:
                if ( CurrentTime - Code.lastTimeID411 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID411 = CurrentTime;
                return false;
            case Code.ID412:
                if ( CurrentTime - Code.lastTimeID412 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID412 = CurrentTime;
                return false;
            case Code.ID413:
                if ( CurrentTime - Code.lastTimeID413 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID413 = CurrentTime;
                return false;
            case Code.ID414:
                if ( CurrentTime - Code.lastTimeID414 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID414 = CurrentTime;
                return false;
            case Code.ID415:
                if ( CurrentTime - Code.lastTimeID415 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID415 = CurrentTime;
                return false;
            case Code.ID416:
                if ( CurrentTime - Code.lastTimeID416 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID416 = CurrentTime;
                return false;
            case Code.ID417:
                if ( CurrentTime - Code.lastTimeID417 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID417 = CurrentTime;
                return false;
            case Code.ID418:
                if ( CurrentTime - Code.lastTimeID418 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID418 = CurrentTime;
                return false;
            case Code.ID420:
                if ( CurrentTime - Code.lastTimeID420 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID420 = CurrentTime;
                return false;
            case Code.ID421:
                if ( CurrentTime - Code.lastTimeID421 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID421 = CurrentTime;
                return false;
            case Code.ID422:
                if ( CurrentTime - Code.lastTimeID422 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID422 = CurrentTime;
                return false;
            case Code.ID423:
                if ( CurrentTime - Code.lastTimeID423 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID423 = CurrentTime;
                return false;
            case Code.ID424:
                if ( CurrentTime - Code.lastTimeID424 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID424 = CurrentTime;
                return false;
            case Code.ID425:
                if ( CurrentTime - Code.lastTimeID425 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID425 = CurrentTime;
                return false;
            case Code.ID426:
                if ( CurrentTime - Code.lastTimeID426 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID426 = CurrentTime;
                return false;
            case Code.ID427:
                if ( CurrentTime - Code.lastTimeID427 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID427 = CurrentTime;
                return false;
            case Code.ID428:
                if ( CurrentTime - Code.lastTimeID428 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID428 = CurrentTime;
                return false;
            case Code.ID430:
                if ( CurrentTime - Code.lastTimeID430 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID430 = CurrentTime;
                return false;
            case Code.ID431:
                if ( CurrentTime - Code.lastTimeID431 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID431 = CurrentTime;
                return false;
            case Code.ID432:
                if ( CurrentTime - Code.lastTimeID432 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID432 = CurrentTime;
                return false;
            case Code.ID433:
                if ( CurrentTime - Code.lastTimeID433 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID433 = CurrentTime;
                return false;
            case Code.ID434:
                if ( CurrentTime - Code.lastTimeID434 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID434 = CurrentTime;
                return false;
            case Code.ID435:
                if ( CurrentTime - Code.lastTimeID435 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID435 = CurrentTime;
                return false;
            case Code.ID436:
                if ( CurrentTime - Code.lastTimeID436 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID436 = CurrentTime;
                return false;
            case Code.ID437:
                if ( CurrentTime - Code.lastTimeID437 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID437 = CurrentTime;
                return false;
            case Code.ID440:
                if ( CurrentTime - Code.lastTimeID440 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID440 = CurrentTime;
                return false;
            case Code.ID442:
                if ( CurrentTime - Code.lastTimeID442 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID442 = CurrentTime;
                return false;
            case Code.ID443:
                if ( CurrentTime - Code.lastTimeID443 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID443 = CurrentTime;
                return false;
            case Code.ID444:
                if ( CurrentTime - Code.lastTimeID444 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID444 = CurrentTime;
                return false;
            case Code.ID445:
                if ( CurrentTime - Code.lastTimeID445 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID445 = CurrentTime;
                return false;
            case Code.ID446:
                if ( CurrentTime - Code.lastTimeID446 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID446= CurrentTime;
                return false;
            case Code.ID447:
                if ( CurrentTime - Code.lastTimeID447 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID447 = CurrentTime;
                return false;
            case Code.ID449:
                if ( CurrentTime - Code.lastTimeID449 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID449 = CurrentTime;
                return false;
            case Code.ID452:
                if ( CurrentTime - Code.lastTimeID452 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID452 = CurrentTime;
                return false;
            case Code.ID454:
                if ( CurrentTime - Code.lastTimeID454 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID454 = CurrentTime;
                return false;
            case Code.IDFA:
                if ( CurrentTime - Code.lastTimeIDFA < IntervalTime) {
                    return true;
                }
                Code.lastTimeIDFA = CurrentTime;
                return false;
            case Code.IDFB:
                if ( CurrentTime - Code.lastTimeIDFB < IntervalTime) {
                    return true;
                }
                Code.lastTimeIDFB = CurrentTime;
                return false;
            case Code.ID79:
                if ( CurrentTime - Code.lastTimeID79 < IntervalTime) {
                    return true;
                }
                Code.lastTimeID79 = CurrentTime;
                return false;
                default:
                    return false;
        }


    }
}
