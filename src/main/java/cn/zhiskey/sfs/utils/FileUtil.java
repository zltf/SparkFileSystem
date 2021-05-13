package cn.zhiskey.sfs.utils;

import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
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

    public static void makeParentFolder(File file) {
        if (!file.getParentFile().exists()) {
            boolean mkdirsRes = file.getParentFile().mkdirs();
            if (!mkdirsRes) {
                new IOException("Can not create " + file.getParentFile().getAbsolutePath() + " folder").printStackTrace();
            }
        }
    }

    public static List<String> makeSpark(File file) throws IOException {
        List<String> sparksHashIDList = new ArrayList<>();
        String fileHashID = "";

        FileInputStream fis = new FileInputStream(file);
        int sparkFileSize = getFileByteSize(ConfigUtil.getInstance().get("sparkFileSize"));
        byte[] fileFragment = new byte[sparkFileSize];
        while ((fis.read(fileFragment))!=-1){
            // 将文件分片hashID加入hashID列表
            String hashIDStr = Base64.getEncoder().encodeToString(HashIDUtil.getHashID(fileFragment));
            sparksHashIDList.add(hashIDStr);
            // 计算全文见hash校验码
            byte[] fileHashIDBytes = HashIDUtil.getHashID(fileHashID + hashIDStr);
            fileHashID = Base64.getEncoder().encodeToString(fileHashIDBytes);
            // 制作文件分片spark
            newSparkFile(hashIDStr, fileFragment);
        }
        fis.close();

        newSeedSparkFile(fileHashID, file.getName(), file.length(), sparksHashIDList);

        sparksHashIDList.add(0, fileHashID);
        return sparksHashIDList;
    }

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
        System.out.println("new spark: " + hashID);
    }

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
        System.out.println("new spark: " + fileHashID);
    }

    public static String getSparkFilePath(String hashID) {
        String filePath = ConfigUtil.getInstance().get("sparkFolder");
        filePath += filePath.charAt(filePath.length()-1) == '/' ? hashID : '/' + hashID;
        filePath += '.' + ConfigUtil.getInstance().get("sparkFileExtension");
        return filePath;
    }

    public static File getSparkFile(String hashID) {
        String filePath = getSparkFilePath(hashID);
        File file = new File(filePath);
        makeParentFolder(file);
        return file;
    }

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
