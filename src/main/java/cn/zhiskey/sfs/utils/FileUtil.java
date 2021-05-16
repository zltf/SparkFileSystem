package cn.zhiskey.sfs.utils;

import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件处理工具类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class FileUtil {
    /**
     * 获取resources目录根路径
     *
     * @return java.lang.String resources目录根路径
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static String getResourcesPath() {
        URL url = FileUtil.class.getResource("/");
        return url == null ? "" : url.getPath();
    }

    /**
     * 为文件创建父目录文件夹
     *
     * @param file 文件对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void makeParentFolder(File file) {
        if (!file.getParentFile().exists()) {
            boolean mkdirsRes = file.getParentFile().mkdirs();
            if (!mkdirsRes) {
                new IOException("Can not create " + file.getParentFile().getAbsolutePath() + " folder").printStackTrace();
            }
        }
    }

    /**
     * 制作某个文件的Spark种子
     *
     * @param file 要制作Spark种子的文件对象
     * @return java.util.List<java.lang.String> 文件的种子HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static List<String> makeSpark(File file) throws IOException {
        List<String> sparksHashIDList = new ArrayList<>();
        String fileHashID = "";

        FileInputStream fis = new FileInputStream(file);
        int sparkFileSize = getFileByteSize(ConfigUtil.getInstance().get("sparkFileSize"));
        byte[] fileFragment = new byte[sparkFileSize];
        while ((fis.read(fileFragment))!=-1){
            // 将文件分片hashID加入hashID列表
            String hashIDStr = HashIDUtil.toString(HashIDUtil.getHashID(fileFragment));
            sparksHashIDList.add(hashIDStr);
            // 计算全文见hash校验码
            byte[] fileHashIDBytes = HashIDUtil.getHashID(fileHashID + hashIDStr);
            fileHashID = HashIDUtil.toString(fileHashIDBytes);
            // 制作文件分片spark
            newSparkFile(hashIDStr, fileFragment);
        }
        fis.close();

        newSeedSparkFile(fileHashID, file.getName(), file.length(), sparksHashIDList);

        sparksHashIDList.add(0, fileHashID);
        return sparksHashIDList;
    }

    /**
     * 将Spark文件恢复为原文件
     *
     * @param seedSparkHashID 种子文件HashID
     * @return java.lang.String 恢复后文件的路径
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static String recoverSpark(String seedSparkHashID) {
        File seedSpark = getSparkFile(seedSparkHashID);
        String path = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(seedSpark));
            String fileName = bufferedReader.readLine();
            path = ConfigUtil.INSTANCE.get("fileFolder") + "/" + fileName;
            int length = Integer.parseInt(bufferedReader.readLine());

            File file = new File(path);
            makeParentFolder(file);
            FileOutputStream fos = new FileOutputStream(file);

            String sparkHashID = bufferedReader.readLine();
            int pos = 0;
            while (sparkHashID != null && !sparkHashID.equals("")) {
                File spark = getSparkFile(sparkHashID);
                FileInputStream fis = new FileInputStream(spark);
                byte[] data = fis.readAllBytes();
                pos += data.length;
                if(pos > length) {
                    int len = data.length - pos + length;
                    byte[] dataLast = new byte[data.length - pos + length];
                    System.arraycopy(data, 0, dataLast, 0, len);
                    data = dataLast;
                }
                fos.write(data);
                fos.flush();

                sparkHashID = bufferedReader.readLine();
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * 创建新Spark文件
     *
     * @param hashID Spark的HashID
     * @param fileFragment 文件碎片的数据字节数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private static void newSparkFile(String hashID, byte[] fileFragment) {
        File file = getSparkFile(hashID);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileFragment);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\tnew spark: " + hashID);
    }

    /**
     * 创建新的Spark种子文件
     *
     * @param fileHashID Spark种子的HashID
     * @param fileName 文件名
     * @param fileLength 文件长度
     * @param sparksHashIDList 文件所有Spark的HashID列表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private static void newSeedSparkFile(String fileHashID, String fileName, long fileLength,List<String> sparksHashIDList) {
        File file = getSparkFile(fileHashID);
        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(fileName);
            bufferedWriter.newLine();
            bufferedWriter.write(String.valueOf(fileLength));
            bufferedWriter.newLine();
            for (String hashID : sparksHashIDList) {
                bufferedWriter.write(hashID);
                bufferedWriter.newLine();
            }

            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\tnew spark: " + fileHashID);
    }

    /**
     * 通过HashID获取Spark文件的路径
     *
     * @param hashID HashID
     * @return java.lang.String 文件路径字符串
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static String getSparkFilePath(String hashID) {
        String filePath = ConfigUtil.getInstance().get("sparkFolder");
        filePath += filePath.charAt(filePath.length()-1) == '/' ? hashID : '/' + hashID;
        filePath += '.' + ConfigUtil.getInstance().get("sparkFileExtension");
        return filePath;
    }

    /**
     * 通过HashID获取Spark文件对象
     *
     * @param hashID HashID
     * @return java.lang.String 文件对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static File getSparkFile(String hashID) {
        String filePath = getSparkFilePath(hashID);
        File file = new File(filePath);
        makeParentFolder(file);
        return file;
    }

    /**
     * 从节点上删除一个Spark文件
     *
     * @param file 要删除的文件对象
     * @param sparkFileList 节点的sparkFileList
     * @param hashID Spark文件的HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void deleteSparkFile(File file, List<String> sparkFileList, String hashID) {
        boolean deleteRes = file.delete();
        if(!deleteRes) {
            new IOException("Can not delete file " + file.getName()).printStackTrace();
        }
        sparkFileList.remove(hashID);
        System.out.println("\tdelete spark: " + hashID);
    }

    /**
     * 将表示文件长度的字符串转换成字节数
     *
     * @param fileSize 表示文件长度的字符串
     * @return int 文件字节数
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private static int getFileByteSize(String fileSize) {
        int num = 0;
        // 提取前面的数字部分
        int pos = 0;
        while (fileSize.charAt(pos) >= '0' && fileSize.charAt(pos) <= '9') {
            num *= 10;
            num += fileSize.charAt(pos) - '0';
            pos++;
        }
        switch (fileSize.substring(pos)) {
            case "b":
            case "B":
            case "byte":
            case "Byte":
                break;
            case "kb":
            case "Kb":
            case "KB":
                num *= 1024;
                break;
            case "mb":
            case "Mb":
            case "MB":
                num *= 1024 * 1024;
                break;
            default:
                new Exception("SparkFileSize error!").printStackTrace();
                break;
        }
        return num;
    }
}
