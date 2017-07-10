package com.jingdong.app.reader.epub.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Kit42Node {
    public ArrayList<Kit42Node> children;
    public Kit42Node parent;
    public String name;
    public String text;
    public Map<String, String> attributeMap;

    public Kit42Node() {
        children = new ArrayList<Kit42Node>();//子节点
        attributeMap = new HashMap<String, String>();//属性Map
    }
    
    /**
     * 合并属性
     * @param map
     */
    public void mergeAttribute(Map<String, String> map) {
        for (String key : map.keySet()) {
            if (attributeMap.containsKey(key)) {
                String value = attributeMap.get(key);
                if ("inherit".equalsIgnoreCase(value)) {
                    attributeMap.put(key, map.get(key));
                }
                continue;
            } else {
                attributeMap.put(key, map.get(key));
            }
        }
    }

    public String toString(int level) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; ++i) {
            sb.append('\t');
        }
        sb.append(name).append(":(");
        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            // append('\n');
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("; ");
        }
        sb.append(")");
        if (name.equals("TEXT")) {
            sb.append(text).append("\n");
        } else {
            sb.append("\n");
        }
        for (Kit42Node node : children) {
            sb.append(node.toString(level + 1));
        }
        return sb.toString();
    }
}
