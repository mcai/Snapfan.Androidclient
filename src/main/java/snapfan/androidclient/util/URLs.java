package snapfan.androidclient.util;

import java.io.Serializable;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 接口URL实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class URLs implements Serializable {
    public final static String HOST = "www.oschina.net";//192.168.1.213  www.oschina.net
    public final static String HTTP = "http://";

    private final static String URL_SPLITTER = "/";

    private final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;
    public final static String NEWS_LIST = URL_API_HOST + "action/api/news_list";
    public final static String NEWS_DETAIL = URL_API_HOST + "action/api/news_detail";

    private final static String URL_HOST = "oschina.net";
    private final static String URL_WWW_HOST = "www." + URL_HOST;

    private final static String URL_TYPE_NEWS = URL_WWW_HOST + URL_SPLITTER + "news" + URL_SPLITTER;

    public final static int URL_OBJ_TYPE_OTHER = 0x000;
    public final static int URL_OBJ_TYPE_NEWS = 0x001;

    private int objId;
    private String objKey = "";
    private int objType;

    public int getObjId() {
        return objId;
    }

    public void setObjId(int objId) {
        this.objId = objId;
    }

    public String getObjKey() {
        return objKey;
    }

    public void setObjKey(String objKey) {
        this.objKey = objKey;
    }

    public int getObjType() {
        return objType;
    }

    public void setObjType(int objType) {
        this.objType = objType;
    }

    /**
     * 转化URL为URLs实体
     *
     * @param path
     * @return 不能转化的链接返回null
     */
    public static URLs parseURL(String path) {
        if (StringUtils.isEmpty(path)) return null;
        path = formatURL(path);
        URLs urls = null;
        String objId = "";
        try {
            URL url = new URL(path);
            //站内链接
            if (url.getHost().contains(URL_HOST)) {
                urls = new URLs();
                //www
                if (path.contains(URL_WWW_HOST)) {
                    //新闻  www.oschina.net/news/27259/mobile-internet-market-is-small
                    if (path.contains(URL_TYPE_NEWS)) {
                        objId = parseObjId(path, URL_TYPE_NEWS);
                        urls.setObjId(StringUtils.toInt(objId));
                        urls.setObjType(URL_OBJ_TYPE_NEWS);
                    }
                    //other
                    else {
                        urls.setObjKey(path);
                        urls.setObjType(URL_OBJ_TYPE_OTHER);
                    }
                }
                //other
                else {
                    urls.setObjKey(path);
                    urls.setObjType(URL_OBJ_TYPE_OTHER);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            urls = null;
        }
        return urls;
    }

    /**
     * 解析url获得objId
     *
     * @param path
     * @param url_type
     * @return
     */
    private static String parseObjId(String path, String url_type) {
        String objId = "";
        int p = 0;
        String str = "";
        String[] tmp = null;
        p = path.indexOf(url_type) + url_type.length();
        str = path.substring(p);
        if (str.contains(URL_SPLITTER)) {
            tmp = str.split(URL_SPLITTER);
            objId = tmp[0];
        } else {
            objId = str;
        }
        return objId;
    }

    /**
     * 对URL进行格式处理
     *
     * @param path
     * @return
     */
    private static String formatURL(String path) {
        if (path.startsWith("http://") || path.startsWith("https://"))
            return path;
        return "http://" + URLEncoder.encode(path);
    }
}
