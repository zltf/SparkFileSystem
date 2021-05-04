package cn.zhiskey.sfs.utils.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * XML读写工具类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class XMLUtil {



    public XMLUtil(String path) {
        Document document;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = new File(path);
            // 判断数据文件是否存在
            if(file.exists()) {
                // 解析xml文件
                document = builder.parse(file);
            } else {
                document = builder.newDocument();
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }


    }
}
