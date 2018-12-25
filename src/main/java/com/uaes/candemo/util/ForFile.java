package com.uaes.candemo.util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ForFile {
    //生成文件路径
    private static String path = "C:\\test\\";

    //文件路径+名称
    private static String filenameTemp;
    private static String filename = "txfileB3.dat";
    private final File fileb3;
    private final File file60;
    private final File file78;
    public static FileOutputStream fosb3  = null;
    public static FileOutputStream fos60  = null;
    public static FileOutputStream fos78  = null;

    public ForFile(String filepathb3,String filepath60,String filepath78) {
        fileb3 = new File(filepathb3);//文件路径(包括文件名称)
        file60 = new File(filepath60);
        file78 = new File(filepath78);
        try {
            fosb3 = new FileOutputStream(fileb3,true);
            fos60 = new FileOutputStream(file60,true);
            fos78 = new FileOutputStream(file78,true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeToB3File(byte[] b){
        try {
            if(fosb3 != null){
                fosb3.write(b);
            }else{
                System.out.println("canId:b3 write failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeTo60File(byte[] b){
        try {
            if(fos60 != null){
                fos60.write(b);
            }else{
                System.out.println("canId:60 write failed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeTo78File(byte[] b){
        try {
            if(fos78 != null){
                fos78.write(b);
            }else{
                System.out.println("canId:78 write failed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 创建文件
     * @param fileName  文件名称
     * @return  是否创建成功，成功则返回true
     */
    public static boolean createFile(String fileName){
        Boolean bool = false;
        filenameTemp = path+fileName;//文件路径+名称+文件类型
        File file = new File(filenameTemp);
        try {
            if(file.exists()){
                file.delete();
                file.createNewFile();
                bool = true;
                System.out.println("success create file,the file is "+filenameTemp);
            }else{
               file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bool;
    }

    /**
     * 向文件中写入内容
     * @param filepath 文件路径与名称
     * @return
     * @throws IOException
     */
    public static boolean writeFileContent(String filepath,String filepath60 ,byte[] b) throws IOException{
        Boolean bool = false;
        FileOutputStream fos  = null;
        FileOutputStream fos60  = null;
        byte[] b3 = new byte[]{1,2,3,4,-77,0,0,0,9,10,11,12,13,14,15,16};
        byte[] b60 = new byte[]{1,2,3,4,0x60,0,0,0,9,10,11,12,13,14,15,16};
        try {
            File file = new File(filepath);//文件路径(包括文件名称)
            File file60 = new File(filepath60);
            fos = new FileOutputStream(file,true);
            fos60 = new FileOutputStream(file60,true);
            fos60.write(b);
//            for (int i = 0;i < 1000;i++){
////                fos.write(content);
//                Thread.sleep(1000);
//                b3[15] = (byte) (b3[15]+1);
//                b60[15] = (byte) (b60[15]+1);
//                System.out.println("byte[16]:" + String.format("%02X", b3[15]));
//                fos.write(b3);
//                fos60.write(b);
//            }
            bool = true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //不要忘记关闭
            if (fos != null) {
                fos.close();
            }
        }
        return bool;
    }

    /**
     * 删除文件
     * @param fileName 文件名称
     * @return
     */
    public static boolean delFile(String fileName){
        Boolean bool = false;
        filenameTemp = path+fileName+".txt";
        File file  = new File(filenameTemp);
        try {
            if(file.exists()){
                file.delete();
                bool = true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return bool;
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        byte[] b = {-127};
        int a = b[0] >> 1;
        System.out.println(a);
//        UUID uuid = UUID.randomUUID();
//        byte[] bs = new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x10,0x11,0x12,0x13,014,0x15,0x15};
//        createFile(filename);
//        filenameTemp = path+filename;//文件路径+名称+文件类型
//        for (int i = 0;i < 1000;i++){
//            writeFileContent(filenameTemp,bs);
//            Thread.sleep(1000);
//
//        }
    }
















}
