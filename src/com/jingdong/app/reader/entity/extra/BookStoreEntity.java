package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 书城数据实体
 */
public class BookStoreEntity  implements Serializable{

	public class Modules implements Comparable<Modules>,Serializable{
		/**
		 * 创建时间
		 */
		public String created;
		/**
		 * 创建人
		 */
		public String creator;
		public int fid;
		public int id;
		/**
		 * 是否展示，0:隐藏; 1:显示
		 */
		public int isShow;
		public String modified;
		/**
		 * 模块类型标识,1: 专题标签-固定; 2: 专题标签-自定义; 3: 分类模块; 4: 关键字模块; 5: 推荐模块; 6: 特辑模块; 7: 单条主题/广告模块; 8: 限时免费; 9: 分类列表页
		 */
		public int moduleType;
		/**
		 * 模块类型英文标识,1专题标签-固定，subjectFix；2专题标签-自定义，subjectDefined；3分类模块，category；4关键字模块，keyword；5推荐模块，recommend；6特辑模块,special；7单条主题/广告模块,advertisement；8限时免费,limitFree；9分类列表页,cateListPage
		 */
		public String moduleTypeEn;
		/**
		 * 模块类型名称
		 */
		public String moduleTypeStr;
		/**
		 * 备注名称
		 */
		public String showName;
		/**
		 * 排序
		 */
		public int sort;
		/**
		 * 状态，返回值中该字段值都为1：上线
		 */
		public int status;
		public String statusStr;
		
		@Override
		public int compareTo(Modules another) {
			        return this.sort > another.sort ? 1  
			                : this.sort < another.sort ? -1 : 0;  
		}
	}

	public class MainThemeList implements Serializable{
		public ArrayList<Modules> modules;
		/**
		 * 创建时间
		 */
		public String created;
		/**
		 * 格式化的创建时间
		 */
		public String createdStr;
		/**
		 * 创建人
		 */
		public String creator;
		public int id;
		/**
		 * 是否展示，0:隐藏; 1:显示
		 */
		public int isShow;
		/**
		 * 修改时间
		 */
		public String modified;
		/**
		 * 格式化修改时间
		 */
		public String modifiedStr;

		/**
		 * 展示名
		 */
		public String showName;
		/**
		 * 排序
		 */
		public int sort;
		/**
		 * 状态，返回值中该字段值都为1：上线
		 */
		public int status;
		public String statusStr;
		/**
		 * 1:Android; 2: iPhone; 3: iPad
		 */
		public int sysId;
	}

	public int code;
	public String message;
	public List<MainThemeList> mainThemeList;

}
