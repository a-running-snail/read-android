package com.jingdong.app.reader.epub.epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;

import com.jingdong.app.reader.epub.FilePath;
import com.jingdong.app.reader.epub.JDDecryptUtil;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.util.MZLog;

public class ContentReader {

    private static final String TAG = "ContentReader";
    private String rootDir;
    private final List<String> spineIdrefList = new ArrayList<String>();//章节信息列表（有顺序）
    private final Map<String, String> manifestMap = new HashMap<String, String>();
    private final List<Spine> spineList = new ArrayList<Spine>();
    private final List<String> cssList = new ArrayList<String>();
    private String coverId = "";
    private String coverPath = "";//ePub封面地址（全路径）
    private String coverImage = "";//ePub封面地址（不全，没有加根路径）
    private String language = "";
    private String title = "";//外部导入的书籍名，书城下载的统一使用后台返回json的书名
    private String author = "";//作者
    private String opfPath = "";
    private String tocId;//OPF的spine节点的toc属性
    private String tocPath;
    private String audioPath;
    /**
     * 目录列表
     */
    private final List<TOCItem> TOCList = new ArrayList<TOCItem>();
    private ArrayList<PlayItem> playList = new ArrayList<PlayItem>();
    /**
     * 是否需要解密
     */
    public static boolean isNeedJDDecrypt = false;
    
    /**
     * ePub文件解析<br />
     * 1、读取并解析container.xml文件，获取rootfile路径<br />
     * 2、解析ePub的OPF文件<br />
     * 3、目录结构信息（NCX文件）<br />
     * 4、其他信息读取<br />
     * @param epubDir ePub文件路径
     * @throws IOException IO异常
     * @throws XmlPullParserException XML解析异常
     */
    public ContentReader(String epubDir) throws IOException, XmlPullParserException {
    	//读取并解析container.xml文件，获取rootfile路径
        String contentFilePath = readContainer(epubDir);
        if (contentFilePath == null) {//epub文件不合法
            return;
        }

        //OPF文件
        File contentFile = new File(epubDir, contentFilePath);
        opfPath = contentFile.getPath();
        rootDir = contentFile.getParent() + File.separator;
        if (!contentFile.exists()) {
            MZLog.d(TAG, "content file: " + contentFile.getPath() + "doesn't exists");
            return;
        }

        //解析ePub的OPF文件
        parseEPubOPFFile(contentFile);
        //解析目录信息
        parseNcxFile();
        //解析可播放文件列表
        parsePlayList();
    }

    /**
     * 解析可播放文件列表
     */
	private void parsePlayList() {
		File playlistFile = new File(rootDir, "Text/playlist.xhtml");
        if (playlistFile.exists()) {
            parsePlaylist(playlistFile);
        }
	}

    /**
     * 解析ePub文件的目录信息
     * @throws IOException
     * @throws XmlPullParserException
     */
	private void parseNcxFile() throws IOException, XmlPullParserException {
		//目录结构信息（NCX文件）
        if (!TextUtils.isEmpty(tocPath)) {
            File tocFile = new File(rootDir, tocPath);
            MZLog.d("ContentReader", "toc path: " + tocFile);
            //解析NCX文件
            readNcxFile(tocFile);
        }
        
        for (Spine spine : spineList) {
            for (TOCItem item : TOCList) {
            	//修正章节名
                if (spine.chapterName == null && item != null  && item.contentSrc != null && item.contentSrc.contains(spine.spinePath)) {
                    spine.chapterName = item.navLabel;
                }
            }
        }
	}
    
    public static String getOpfPath(String epubDir) throws IOException, XmlPullParserException {
        String contentFilePath = readContainer(epubDir);
        if (contentFilePath == null) {
            MZLog.d(TAG, "can not get contentfile path from META-INF/container.xml");
            return null;
        }

        File contentFile = new File(epubDir, contentFilePath);
        return contentFile.getPath();// return opfPath
    }

    private void parsePlaylist(File playlistFile) {

        try {
            InputStream is = null;
            if (isNeedJDDecrypt) {
                is = JDDecryptUtil.decryptFile(playlistFile.getPath());
            } else {
                is = new FileInputStream(playlistFile);
            }
            if (is == null) {
                return;
            }

            InputSource source = new InputSource(is);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String expression = "//*[@class='mz-audio-play-list']";
            Node node = (Node) xpath.compile(expression).evaluate(source, XPathConstants.NODE);
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node child = nodeList.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    PlayItem item = new PlayItem();
                    Element element = (Element) child;
                    item.id = element.getAttribute("id");
                    NodeList elementChildren = element.getChildNodes();
                    for (int j = 0; j < elementChildren.getLength(); ++j) {
                        Node entryNode = elementChildren.item(j);
                        if (entryNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element entry = (Element) entryNode;
                            String className = entry.getAttribute("class");
                            if (className.equals("track-title")) {
                                item.title = entry.getTextContent();
                            } else if (className.equals("track-link")) {
                                Element link = (Element) entry.getChildNodes().item(0);
                                item.navSrc = FilePath.resolveRelativePath(playlistFile.getParent() + File.separator,
                                        link.getAttribute("href"));
                                item.navTitle = link.getTextContent();
                            } else if (className.equals("track-author")) {
                                item.author = entry.getTextContent();
                            }
                            
                            if (entry.getNodeName().equals("audio")) {
                                String src = entry.getAttribute("src");
                                Uri uri = Uri.parse(src);
                                if (uri.isRelative()) {
                                    item.mediaPath = FilePath.resolveRelativePath(playlistFile.getParent() + File.separator,
                                            src);
                                    if (TextUtils.isEmpty(audioPath)) {
                                        File file = new File(item.mediaPath);
                                        audioPath = file.getParent() + File.separator; 
                                    }
                                } else {
                                    item.mediaPath = src;
                                }
                            }
                        }
                    }
                    playList.add(item);
                }
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public String getAutioPath() {
        if (TextUtils.isEmpty(audioPath)) {
            return rootDir + "Audio" + File.separator;
        } else {
            return audioPath;
        }
    }

    public List<Spine> getSpineList() {
        return spineList;
    }

    public List<String> getCssList() {
        return cssList;
    }

    /**
     * 获取ePub封面地址
     * @return 目录地址
     */
    public String getCoverPath() {
        return coverPath;
    }
    
    public String getCssPath() {
        if (cssList != null && cssList.size() > 0) {
            return cssList.get(0);
        }
        return "";
    }

    public String getLanguage() {
        return language;
    }

    public String getTitle() {
        return title;//外部导入的书籍名，书城下载的统一使用后台返回json的书名
    }

    public String getAuthor() {
        return author;
    }

    public String getOpfPath() {
        return opfPath;
    }

    public List<TOCItem> getTOCList() {
        return TOCList;
    }
    
    public ArrayList<PlayItem> getPlayList() {
        return playList;
    }

    /**
     * 读取ePub的Container文件中的根文件路径
     * container.xml的主要功能用于告诉阅读器，电子书的根文件（rootfile）的路径（红色部分）和打开放式，
     * @param epubDir ePub文件路径
     * @return 
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static String readContainer(String epubDir) throws IOException, XmlPullParserException {
        InputStream in = null;
        //是否需要解密
        if (isNeedJDDecrypt) {
        	//解密文件， container.xml的主要功能用于告诉阅读器，
        	//电子书的根文件（rootfile）的路径（红色部分）和打开放式，
            in = JDDecryptUtil.decryptFile(epubDir+"/META-INF/container.xml");
        } else {
        	//不需要解密
            File container = new File(epubDir, "META-INF/container.xml");
            in = new FileInputStream(container);
        }
        if (in == null) {
            return null;
        }
        try {
        	
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, "container");

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the rootfile tag
                if (name.equals("rootfile")) {
                    return parser.getAttributeValue(null, "full-path");
                }
            }
        } finally {
            IOUtil.closeStream(in);
        }
        return null;
    }

    /**
     * 解析ePub OPF文件信息（元数据、文件列表、章节顺序）
     * @param file 文件
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void parseEPubOPFFile(File file) throws IOException, XmlPullParserException {
        InputStream in = null;
        if (isNeedJDDecrypt) {
        	//DRM解密
            in = JDDecryptUtil.decryptFile(file.getPath());
        } else {
            in = new FileInputStream(file);
        }
        if (in == null) {
            return;
        }
        try {
        	//解析OPF文件中的XML内容
            parseOpfXmlContent(in);
            //填充章节列表所引用的文件
            fillTheItemToSpineList();
            //整理图书封面路径信息
            buildCoverPath(coverId);
            if (!TextUtils.isEmpty(tocId)) {//从文件列表中找到ncx文件路径
                tocPath = manifestMap.get(tocId);
            }
        } finally {
            IOUtil.closeStream(in);
        }
    }

	private void fillTheItemToSpineList() {
		int index = 0;
		for (String id : spineIdrefList) {
		    String path = manifestMap.get(id);
		    if (path != null) {
		        spineList.add(new Spine(id, rootDir + path, index));
		        index ++;
		    } else {
		        // never to here
		    }
		}
	}

	/**
	 * 整理图书封面路径信息
	 */
	private void buildCoverPath(String coverId) {
		if (!TextUtils.isEmpty(coverId)) {
		    String path = manifestMap.get(coverId);
		    if (!TextUtils.isEmpty(path)) {
		        coverPath = rootDir + path;
		    }
		}
		if (TextUtils.isEmpty(coverPath)) {
		    // 标准格式在meta中会有coverId的，第三方导入的会可能出现没有的情况，此时尝试读取manifest下的包含cover的图片
		    if (!TextUtils.isEmpty(coverImage)) {
		        coverPath = rootDir + coverImage;
		    }
		}
	}

	/**
	 * 解析OPF文件中的XML内容<br />
	 * 1、解析文件列表节点（manifest）<br />
	 * 2、解析章节顺序信息节点（spine）<br />
	 * 3、解析元数据信息节点（metadata）<br />
	 * @param in
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void parseOpfXmlContent(InputStream in) throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		parser.setInput(in, null);
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "package");

		while (parser.next() != XmlPullParser.END_TAG) {
		    if (parser.getEventType() != XmlPullParser.START_TAG) {
		        continue;
		    }
		    //节点名字
		    String name = parser.getName();
		    if (name.equals("manifest")) {
		    	//读取文件列表信息
		        readManifest(parser);
		    } else if (name.equals("spine")) {
		    	//解析章节顺序结构
		        readSpine(parser);
		    } else if (name.equals("metadata")) {
		    	//读取元数据信息
		        readMetadata(parser);
		    } else {
		    	//跳过
		        skip(parser);
		    }
		}
	}

    /**
     * 读取OPF文件中的文件列表节点（节点名字manifest）
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readManifest(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "manifest");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("item")) {//文件列表
                readManifestItem(parser);
            } else if (name.equals("spine")) {
                readSpine(parser);//含tocId，spine标签含toc属性
            } else {
                skip(parser);
            }
        }
    }

    /**
     * 解析epub文件中的OPF文件的manifest节点的item信息<br />
     * item含css,html，image等，顺序不是章节展示顺序<br />
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
	private void readManifestItem(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, null, "item");
		String id = parser.getAttributeValue(null, "id");
		String path = parser.getAttributeValue(null, "href");//
		String mediaType = parser.getAttributeValue(null, "media-type");//类型
		String properties = parser.getAttributeValue(null, "properties");
		if (id != null && path != null) {
		    manifestMap.put(id, path);//文件列表
		    //若是CSS的话加入CSS列表
		    if ("text/css".equals(mediaType)) {
		        cssList.add(rootDir + path);//样式列表
		    }
		    
		    //解析封面图路径
			if ("image/jpeg".equals(mediaType)) {
			    if (!TextUtils.isEmpty(id) && (id.contains("cover") || id.contains("Cover") || id.contains("COVER"))) {
			        coverImage = path;
			    }
			    if (!TextUtils.isEmpty(properties) && properties.equalsIgnoreCase("cover-image")) {
			        coverImage = path;
			    }
			}
		}
		parser.nextTag();
		parser.require(XmlPullParser.END_TAG, null, "item");
	}

    /**
     * 读取OPF文件中的Spine（章节文件顺序结构，即HTML文件顺序）
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readSpine(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "spine");
        //目录文件（对应文件清单节点manifest中的ID）
        tocId = parser.getAttributeValue(null, "toc");//
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("itemref")) {
                parser.require(XmlPullParser.START_TAG, null, "itemref");
                String idref = parser.getAttributeValue(null, "idref");
                if (idref != null) {
                    spineIdrefList.add(idref);//章节顺序
                }
                parser.nextTag();
                parser.require(XmlPullParser.END_TAG, null, "itemref");
            } else {
                skip(parser);
            }
        }
    }

    /**
     * 解析OPF文档元数据
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readMetadata(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "metadata");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("meta")) {
                parser.require(XmlPullParser.START_TAG, null, "meta");
                String metaName = parser.getAttributeValue(null, "name");
                String content = parser.getAttributeValue(null, "content");

                if (metaName != null && content != null && metaName.equals("cover")) {
                    coverId = content;
                }

                while (parser.next() != XmlPullParser.END_TAG) {
                    continue;
                }
                // parser.nextTag();
                parser.require(XmlPullParser.END_TAG, null, "meta");
            } else if (name.equals("title")) {
                parser.require(XmlPullParser.START_TAG, null, "title");
                title = readText(parser);
                parser.require(XmlPullParser.END_TAG, null, "title");
            } else if (name.equals("language")) {
                parser.require(XmlPullParser.START_TAG, null, "language");
                language = readText(parser);
                parser.require(XmlPullParser.END_TAG, null, "language");
            } else if (name.equals("creator")) {
                parser.require(XmlPullParser.START_TAG, null, "creator");
                author = readText(parser);
                parser.require(XmlPullParser.END_TAG, null, "creator");
            } else {
                skip(parser);
            }
        }
    }

    /**
     * 解析ePub的NCX文件(目录结构)
     * @param ncxFile
     * @throws IOException
     * @throws XmlPullParserException
     */
    private void readNcxFile(File ncxFile) throws IOException, XmlPullParserException {
        InputStream in = null;
        if (isNeedJDDecrypt) {
            in = JDDecryptUtil.decryptFile(ncxFile.getPath());
        } else {
            in = new FileInputStream(ncxFile);
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, null, "ncx");

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the rootfile tag
                if (name.equals("navMap")) {
                    readNavMap(parser);
                } else {
                    skip(parser);
                }
            }
        } catch(Throwable t) {
            t.printStackTrace();
        } finally {
            IOUtil.closeStream(in);
        }
    }

    /**
     * 解析NCX文件中的navMap节点（章节目录）
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readNavMap(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "navMap");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the rootfile tag
            if (name.equals("navPoint")) {
                readNavPoint(parser, 0);
            } else {
                skip(parser);
            }
        }

    }

    /**
     * 解析NCX文件中的navMap节点下的navPoint（章节目录）
     * @param parser
     * @param level
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void readNavPoint(XmlPullParser parser, int level) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "navPoint");
        String contentSrc = "";
        String navLabel = "";

        //目录条目
        TOCItem tocItem = new TOCItem();
        TOCList.add(tocItem);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the rootfile tag
            if (name.equals("navPoint")) {
                readNavPoint(parser, level + 1);//层级加1
            } else if (name.equals("content")) {
                contentSrc = rootDir + parser.getAttributeValue(null, "src");
                while (parser.next() != XmlPullParser.END_TAG) {
                    continue;
                }
                parser.require(XmlPullParser.END_TAG, null, "content");
            } else if (name.equals("navLabel")) {
                navLabel = readNavLabel(parser);
            } else {
                skip(parser);
            }
        }
        // TOCItem tocItem = new TOCItem(navLabel, contentSrc, level);
        tocItem.navLabel = navLabel;
        tocItem.contentSrc = contentSrc;
        tocItem.level = level;
        parser.require(XmlPullParser.END_TAG, null, "navPoint");

    }

    private String readNavLabel(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "navLabel");
        String label = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the rootfile tag
            if (name.equals("text")) {
                label = readText(parser);
            } else {
                skip(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, null, "navLabel");
        return label;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * 跳过当前节点
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                depth--;
                break;
            case XmlPullParser.START_TAG:
                depth++;
                break;
            }
        }
    }

}
