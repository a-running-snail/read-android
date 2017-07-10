package com.jingdong.app.reader.epub.css;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextUtils;

public class CSSFont {

    private String cssPath;//css样式文件的绝对路径
    private String fontFamily;
    private String srcNormalNormal;
    private String srcNormalBold;
    private String srcItalicNormal;
    private String srcItalicBold;
    private Typeface typeFaceNN;
    private Typeface typeFaceNB;
    private Typeface typeFaceIN;
    private Typeface typeFaceIB;

    public void setCssPath(String cssPath) {
        if (cssPath == null) {
            this.cssPath = "";
            return;
        }
        if (cssPath.endsWith(".css")) {
            int index = cssPath.lastIndexOf("/");
            if (index > 0) {
                cssPath = cssPath.substring(0, index + 1);
            }
        }
        this.cssPath = cssPath;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public String getSrcNormalNormal() {
        return srcNormalNormal;
    }

    public void setSrcNormalNormal(String srcNormalNormal) {
        this.srcNormalNormal = srcNormalNormal;
    }

    public String getSrcNormalBold() {
        return srcNormalBold;
    }

    public void setSrcNormalBold(String srcNormalBold) {
        this.srcNormalBold = srcNormalBold;
    }

    public String getSrcItalicNormal() {
        return srcItalicNormal;
    }

    public void setSrcItalicNormal(String srcItalicNormal) {
        this.srcItalicNormal = srcItalicNormal;
    }

    public String getSrcItalicBold() {
        return srcItalicBold;
    }

    public void setSrcItalicBold(String srcItalicBold) {
        this.srcItalicBold = srcItalicBold;
    }

    public Typeface getTypeFaceNN() {
        return typeFaceNN;
    }

    public Typeface getTypeFaceNB() {
        return typeFaceNB;
    }

    public Typeface getTypeFaceIN() {
        return typeFaceIN;
    }

    public Typeface getTypeFaceIB() {
        return typeFaceIB;
    }
    
    public void setTypeface(String fontStyle, String fontWeight, Paint paint) {
        if ("italic".equalsIgnoreCase(fontStyle)) {
            if ("bold".equalsIgnoreCase(fontWeight)) {
                if (typeFaceIB != null) {
                    paint.setTypeface(typeFaceIB);
                }
            } else {
                if (typeFaceIN != null) {
                    paint.setTypeface(typeFaceIN);
                }
            }
        } else {
            if ("bold".equalsIgnoreCase(fontWeight)) {
                if (typeFaceNB != null) {
                    paint.setTypeface(typeFaceNB);
                }
            } else {
                if (typeFaceNN != null) {
                    paint.setTypeface(typeFaceNN);
                }
            }
        }
    }
    
    public void generateTypeface(String fontStyle, String fontWeight) {
        if ("italic".equalsIgnoreCase(fontStyle)) {
            if ("bold".equalsIgnoreCase(fontWeight)) {
                if (typeFaceIB == null && !TextUtils.isEmpty(srcItalicBold)) {
                    String[] srcIB = srcItalicBold.split(",");
                    for (int i = 0; i < srcIB.length; i++) {
                        if (srcIB[i].startsWith("url(")) {
                            String src = srcIB[i].substring(4,
                                    srcIB[i].length() - 1);
                            if (src.startsWith("res:///")) {

                            } else if (src.startsWith("../")) {
                                int index = cssPath.lastIndexOf("/");
                                if (index > 0) {
                                    String path = cssPath.substring(0, index + 1);
                                    src = path + src;
                                } else {
                                    src = cssPath + src;
                                }
                            } else {
                                src = cssPath + src;
                            }
                            try {
                                typeFaceIB = Typeface.createFromFile(src);
                            } catch (Exception e) {
                            }
                            if (typeFaceIB != null) {
                                break;
                            }
                        }
                    }
                }
            } else {
                if (typeFaceIN == null && !TextUtils.isEmpty(srcItalicNormal)) {
                    String[] srcIN = srcItalicNormal.split(",");
                    for (int i = 0; i < srcIN.length; i++) {
                        if (srcIN[i].startsWith("url(")) {
                            String src = srcIN[i].substring(4,
                                    srcIN[i].length() - 1);
                            if (src.startsWith("res:///")) {

                            } else if (src.startsWith("../")) {
                                int index = cssPath.lastIndexOf("/");
                                if (index > 0) {
                                    String path = cssPath.substring(0, index + 1);
                                    src = path + src;
                                } else {
                                    src = cssPath + src;
                                }
                            } else {
                                src = cssPath + src;
                            }
                            try {
                                typeFaceIN = Typeface.createFromFile(src);
                            } catch (Exception e) {
                            }
                            if (typeFaceIN != null) {
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            if ("bold".equalsIgnoreCase(fontWeight)) {
                if (typeFaceNB == null && !TextUtils.isEmpty(srcNormalBold)) {
                    String[] srcNB = srcNormalBold.split(",");
                    for (int i = 0; i < srcNB.length; i++) {
                        if (srcNB[i].startsWith("url(")) {
                            String src = srcNB[i].substring(4,
                                    srcNB[i].length() - 1);
                            if (src.startsWith("res:///")) {

                            } else if (src.startsWith("../")) {
                                int index = cssPath.lastIndexOf("/");
                                if (index > 0) {
                                    String path = cssPath.substring(0, index + 1);
                                    src = path + src;
                                } else {
                                    src = cssPath + src;
                                }
                            } else {
                                src = cssPath + src;
                            }
                            try {
                                typeFaceNB = Typeface.createFromFile(src);
                            } catch (Exception e) {
                            }
                            if (typeFaceNB != null) {
                                break;
                            }
                        }
                    }
                }
            } else {
                if (typeFaceNN == null && !TextUtils.isEmpty(srcNormalNormal)) {
                    String[] srcNN = srcNormalNormal.split(",");
                    for (int i = 0; i < srcNN.length; i++) {
                        if (srcNN[i].startsWith("url(")) {
                            String src = srcNN[i].substring(4,
                                    srcNN[i].length() - 1);
                            if (src.startsWith("res:///")) {

                            } else if (src.startsWith("../")) {
                                int index = cssPath.lastIndexOf("/");
                                if (index > 0) {
                                    String path = cssPath.substring(0, index + 1);
                                    src = path + src;
                                } else {
                                    src = cssPath + src;
                                }
                            } else {
                                src = cssPath + src;
                            }
                            try {
                                typeFaceNN = Typeface.createFromFile(src);
                            } catch (Exception e) {
                            }
                            if (typeFaceNN != null) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static boolean isFZSSFont(String fontFamily) {
        return ("宋体".equals(fontFamily) ||
                "SimSun".equalsIgnoreCase(fontFamily) ||
                "Simsong".equalsIgnoreCase(fontFamily) ||
                "songti".equalsIgnoreCase(fontFamily) ||
                "song".equalsIgnoreCase(fontFamily) ||
                "_GBK".equalsIgnoreCase(fontFamily));
    }
    
    public static boolean isFZLTHFont(String fontFamily) {
        return ("黑体".equals(fontFamily) ||
                "SimHei".equalsIgnoreCase(fontFamily) ||
                "HiFont Hei GB".equalsIgnoreCase(fontFamily) ||
                "heiti".equalsIgnoreCase(fontFamily) ||
                "hei".equalsIgnoreCase(fontFamily) ||
                "FZLanTingHei-R-GB18030".equalsIgnoreCase(fontFamily));
    }
    
    public static boolean isFZKTFont(String fontFamily) {
        return ("楷体".equals(fontFamily) ||
                "Kaiti".equalsIgnoreCase(fontFamily) ||
                "FZKai-Z03S".equalsIgnoreCase(fontFamily) ||
                "FZKai-Z03".equalsIgnoreCase(fontFamily));
    }
}
