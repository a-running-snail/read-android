package com.jingdong.app.reader.epub.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.jingdong.app.reader.io.IOUtil;

import android.util.Log;
import android.util.Xml;

public class Kit42 {
    static final String TAG = "Kit42";
    static private int olNestDepth = 0;
    static private int ulNestDepth = 0;

    static public Kit42Node parse(String text) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader (text));
            parser.nextTag();
            return parseNodeTree(parser, null);
        } finally {
           
        }
    }
    
    static public Kit42Node parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return parseNodeTree(parser, null);
        } finally {
            IOUtil.closeStream(in);
        }
    }

    static private Kit42Node parseNodeTree(XmlPullParser parser, Kit42Node parentNode) throws IOException,
            XmlPullParserException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            Log.e(TAG, "parserNodeTree should begin with start tag");
        }
        Kit42Node node = new Kit42Node();
        node.parent = parentNode;
        node.name = parser.getName();

        if (node.name.equalsIgnoreCase("br")) {
            Kit42Node brNode = new Kit42Node();
            brNode.name = "TEXT";
            brNode.text = "\n\u200B";
            node.children.add(brNode);
        }

        if (node.name.equalsIgnoreCase("wbr")) {
            Kit42Node wbrNode = new Kit42Node();
            wbrNode.name = "TEXT";
            wbrNode.text = "\u2009";
            node.children.add(wbrNode);
        }

        if (node.name.equalsIgnoreCase("rt")) {
            Kit42Node rtNode = new Kit42Node();
            rtNode.name = "TEXT";

            if (parser.getEventType() == XmlPullParser.START_TAG)
                rtNode.text = "[";
            else
                rtNode.text = "]";

            node.children.add(rtNode);
        }

        if (node.name.equalsIgnoreCase("li")) {
            Kit42Node liNode = new Kit42Node();
            liNode.name = "TEXT";

            String leadText = "";
            if (node.parent.name.equalsIgnoreCase("ol") && olNestDepth > 1) {
                for (int step = 1; step < olNestDepth; step++)
                    leadText += "\u3000\u3000";
            }

            if (node.parent.name.equalsIgnoreCase("ul") && ulNestDepth > 1) {
                for (int step = 1; step < ulNestDepth; step++)
                    leadText += "\u3000\u3000";
            }

            if (node.parent.name.equalsIgnoreCase("ul"))
                liNode.text = "\n\u200B" + leadText + "\u2022\u0020";
            else
                liNode.text = "\n\u200B" + leadText;
            node.children.add(liNode);
        }

        if (node.name.equalsIgnoreCase("dd")) {
            Kit42Node liNode = new Kit42Node();
            liNode.name = "TEXT";
            liNode.text = "\n\u200B\u0020\u0020";
            node.children.add(liNode);
        }

        if (node.name.equalsIgnoreCase("ol")) {
            if (parser.getEventType() == XmlPullParser.START_TAG)
                olNestDepth += 1;
            else
                olNestDepth -= 1;
        }

        if (node.name.equalsIgnoreCase("ul")) {
            if (parser.getEventType() == XmlPullParser.START_TAG)
                ulNestDepth += 1;
            else
                ulNestDepth -= 1;
        }

        parseAttribute(parser, node);

        while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() == XmlPullParser.END_DOCUMENT
					&& parser.next() == XmlPullParser.END_DOCUMENT) {
				break;
			}
			
            if (parser.getEventType() == XmlPullParser.TEXT && !parser.isWhitespace()) {
                Kit42Node textNode = new Kit42Node();
                textNode.parent = node;

                String[] segments = parser.getText().split("\n");
                String   lecture = "";

                for (String segment : segments) {
                    lecture += segment.trim() + " ";
                }

                textNode.text = lecture.trim();
                textNode.name = "TEXT";

                node.children.add(textNode);
            }

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            
            Kit42Node child = parseNodeTree(parser, node);
            node.children.add(child);
        }

        if (node.name.equalsIgnoreCase("rt")) {
            Kit42Node rtNode = new Kit42Node();
            rtNode.name = "TEXT";

            if (parser.getEventType() == XmlPullParser.START_TAG)
                rtNode.text = "[";
            else
                rtNode.text = "]";

            node.children.add(rtNode);
        }

        if (node.name.equalsIgnoreCase("ol")) {
            if (parser.getEventType() == XmlPullParser.START_TAG)
                olNestDepth += 1;
            else
                olNestDepth -= 1;
        }

        if (node.name.equalsIgnoreCase("ul")) {
            if (parser.getEventType() == XmlPullParser.START_TAG)
                ulNestDepth += 1;
            else
                ulNestDepth -= 1;
        }

        return node;

    }

    static private void parseAttribute(XmlPullParser parser, Kit42Node node) {
        int attributeCount = parser.getAttributeCount();

        for (int i = 0; i < attributeCount; ++i) {
            node.attributeMap.put(parser.getAttributeName(i), parser.getAttributeValue(i));
        }
    }

    private Kit42() {
    }
}
