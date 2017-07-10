package com.jingdong.app.reader.service;

import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.reading.ReadNote;

public class ReadNoteSyncService {

	public static JSONObject buildActionNode(ReadNote note) throws JSONException {
        JSONObject obj = new JSONObject();
        if (note.serverId == -1) {
            buildCreateNode(obj, note);
        } else if (note.deleted) {
            buildDeleteNode(obj, note);
        } else {
            buildUpdateNode(obj, note);
        }

        return obj;
    }

    private static void buildCreateNode(JSONObject obj, ReadNote note) throws JSONException {
        obj.put("action", "create");
        obj.put("chapter_name", filterSpecialCharactor(note.chapterName));
        if (note.docBindId != 0) {
            obj.put("document_id", note.docBindId);
        } else if (note.ebookId != 0) {
            obj.put("book_id", note.ebookId);
        }
        obj.put("chapter_itemref", note.spineIdRef);
        obj.put("quote_text", filterSpecialCharactor(note.quoteText));
        obj.put("is_private", note.isPrivate ? 1 : 0);
        obj.put("written_at", note.updateTime / 1000);
        obj.put("from_para_index", note.fromParaIndex);
        obj.put("from_offset_in_para", note.fromOffsetInPara);
        obj.put("to_para_index", note.toParaIndex);
        obj.put("to_offset_in_para", note.toOffsetInPara);
        obj.put("content", filterSpecialCharactor(note.contentText));
    }

    private static void buildDeleteNode(JSONObject obj, ReadNote note) throws JSONException {
        obj.put("action", "destroy");
        obj.put("id", note.serverId);
    }

    private static void buildUpdateNode(JSONObject obj, ReadNote note) throws JSONException {
        obj.put("action", "update");
        obj.put("id", note.serverId);
        obj.put("chapter_name", filterSpecialCharactor(note.chapterName));
        if (note.docBindId != 0) {
            obj.put("document_id", note.docBindId);
        } else if (note.ebookId != 0) {
            obj.put("book_id", note.ebookId);
        }
        obj.put("chapter_itemref", note.spineIdRef);
        obj.put("quote_text", filterSpecialCharactor(note.quoteText));
        obj.put("is_private", note.isPrivate ? 1 : 0);
        obj.put("written_at", note.updateTime / 1000);
        obj.put("from_para_index", note.fromParaIndex);
        obj.put("from_offset_in_para", note.fromOffsetInPara);
        obj.put("to_para_index", note.toParaIndex);
        obj.put("to_offset_in_para", note.toOffsetInPara);
        obj.put("content", filterSpecialCharactor(note.contentText));
    }
    
    /**
     * 笔记不替换特殊字符会同步失败。
     * @param note
     * @return
     */
    private static String filterSpecialCharactor(String note) {
    	if (note == null) {
    		return "";
    	}
    	return note.replace(";", "；").replace("%", "％").replace("&", " ");
    }

//    protected void onHandleIntent(Intent intent) {
//        String userId = intent.getStringExtra(UserIdKey);
//        if (TextUtils.isEmpty(userId)) {
//            return;
//        }
//        List<ReadNote> noteList = MZBookDatabase.instance.listAllUnsyncReadNote(userId);
//        
//        if (null==noteList||(null!=noteList&&noteList.size() == 0)) {
//            return;
//        }
//
//        String token = LocalUserSetting.getToken(this);
//        String param = "?auth_token=" + token;
//        String url = URLText.pushNotesUrl + param;
//
//        try {
//            JSONArray arr = new JSONArray();
//            for (ReadNote note : noteList) {
//                arr.put(buildActionNode(note));
//            }
//            JSONObject o = new JSONObject();
//            o.put("notes", arr);
//
//            String postText = "notes=" + o.toString();
//            String result = WebRequest.postWebDataWithContext(this, url, postText);
//            JSONArray resultArr = new JSONArray(result);
//            for (int i = 0; i < resultArr.length(); ++i) {
//                ReadNote note = noteList.get(i);
//                
//                JSONObject resultObj = resultArr.getJSONObject(i);
//                JSONObject errorObj = resultObj.optJSONObject("error");
//                if (errorObj != null) {
//                    int errorCode = errorObj.optInt("code");
//                    if (errorCode == 404) {
//                        note.deleted = true;
//                        note.modified = false;
//                        continue;
//                    }
//                }
//                        
//                boolean success = resultObj.optBoolean("success");
//                if (success) {
//                    if (note.serverId == -1) {
//                        long id = resultObj.optLong("id", -1);
//                        if (id != -1) {
//                            note.serverId = id;
//                            note.modified = false;
//                        }
//                    } else if (note.deleted) {
//                        note.modified = false;
//                    } else {
//                        note.modified = false;
//                    }
//                }
//            }
//            for (ReadNote note : noteList) {
//                MZBookDatabase.instance.insertOrUpdateEbookNote(note);
//            }
//            MZBookDatabase.instance.cleanReadNote();
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

}
