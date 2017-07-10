package com.jingdong.app.reader.epub.paging;

public final class LineBreaker {
    static {
        System.loadLibrary("unibreak-v1");
        init();
    }

    public static final char MustBreak = 0;
    public static final char AllowBreak = 1;

    private static native void init();

    private static native void setLineBreaksForString(String data, String lang, byte[] breaks);

    private final String myLanguage;

    public LineBreaker(String lang) {
        myLanguage = lang;
    }

    public void setLineBreaks(String data, byte[] breaks) {
        setLineBreaksForString(data, myLanguage, breaks);
    }
}
