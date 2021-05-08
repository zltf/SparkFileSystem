package cn.zhiskey.sfs.utils;

import cn.zhiskey.sfs.utils.config.ConfigUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * XML读写工具类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class XMLUtil {
    /**
     * 解析XML文件，获得document对象
     *
     * @param file XML文件file对象
     * @return org.w3c.dom.Document 解析得到的document对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static Document parse(File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(file);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    /**
     * 解析XML文件，获得document对象
     *
     * @param path XML文件路径
     * @return org.w3c.dom.Document 解析得到的document对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static Document parse(String path){
        return parse(new File(path));
    }

    /**
     * 将document对象保存到file文件对象里
     *
     * @param document 要保存的document对象
     * @param target 要保存到的目标文件
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void save(Document document, File target){
        try {
            FileOutputStream fos = new FileOutputStream(target);
            TransformerFactory tff = TransformerFactory.newInstance();
            Transformer tf = tff.newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, ConfigUtil.getInstance().get("xmlEncoding"));
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.transform(new DOMSource(document), new StreamResult(fos));
            fos.close();
        } catch (TransformerException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将document对象保存到指定路径
     *
     * @param document 要保存的document对象
     * @param path 要保存到的目标文件路径
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void save(Document document, String path){
        save(document, new File(path));
    }

    /**
     * 创建一个document对象
     *
     * @return org.w3c.dom.Document 创建的document对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static Document create() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }
}
