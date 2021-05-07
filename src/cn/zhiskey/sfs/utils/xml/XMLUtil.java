package cn.zhiskey.sfs.utils.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
     * @param path XML文件路径
     * @return org.w3c.dom.Document 解析得到的document对象
     * @throws IOException 当文件IO异常时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static Document parse(String path) throws IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            File file = new File(path);
            return db.parse(file);
        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将document对象保存到file文件对象里
     *
     * @param document 要保存的document对象
     * @param target 要保存到的目标文件
     * @throws IOException 当文件IO异常时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void save(Document document, File target) throws IOException {
        FileOutputStream fos = new FileOutputStream(target);
        try {
            TransformerFactory tff = TransformerFactory.newInstance();
            Transformer tf = tff.newTransformer();
            tf.transform(new DOMSource(document), new StreamResult(fos));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        fos.close();
    }

    /**
     * 将document对象保存到指定路径
     *
     * @param document 要保存的document对象
     * @param path 要保存到的目标文件路径
     * @throws IOException 当文件IO异常时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void save(Document document, String path) throws IOException  {
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
        }
        return null;
    }
}
