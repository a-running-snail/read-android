package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;
import java.util.List;

import android.R.integer;

public class BookStoreModuleBookListEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1211378587312413376L;

	// 获取的更多的数据 书籍
	public class BookStoreList implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4146893229026014932L;
		public int amount;
		public int code;
		public int currentPage;
		public int totalPage;
		public List<StoreBook> resultList;

	}

	public class ModuleBookChild implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1970887734973014547L;
		public String created;
		public String creator;
		public int fid;
		public int ftype;
		public int id;
		public int isShow;
		public String modified;
		public String picAddress;
		public int showForm;
		public String showInfo;
		public String showName;
		public int showNum;
		public int showType;// 1:全封面; 2:全列表;

		public String note;
		public String picAddressAll;

	}

	public class AdvResourceList implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2834375783478911368L;
		public String address;
		public String addressAll;
		public String created;
		public int fid;
		public int id;
		public String modified;
		public int status;
		public int type;
	}

	public class ModuleLinkChildList implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6423185962620421745L;
		public String created;
		public int fid;
		public int id;
		public int isShow;
		public String modified;
		public int relateType;
		public String relateTypeStr;
		public int rtype;
		public String rtypeEn;
		public String rtypeStr;
		public String showName;
		public int sort;
		public int status;
		public String relateLink;

		// banner 不同尺寸图片
		public List<AdvResourceList> advResourceList;

		// 部分数据没有
		public ModuleBookChild moduleBookChild;
		public String picAddress;
		public String picAddressAll;
	}

	public class ModuleChildList implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5838152928934027181L;
		public int childType;
		public int childTypeId;
		public String childTypeName;
		public String created;
		public String creator;
		public int fid;
		public int id;
		public int isShow;
		public String modified;
		public String picAddress;
		public int sort;
		public int status;
	}

	public List<StoreBook> resultList;// 书籍列表/排行中有
	public ModuleBookChild moduleBookChild;// 书籍列表/排行中有 5 6 分两种（网格+列表）
	public List<ModuleLinkChildList> moduleLinkChildList;// 1，2，7
	public List<ModuleChildList> moduleChildList;// 顶部圆圈才有的数据结构 type 3/4

	public int totalPage;
	public int amount;
	public String code;
	public int currentPage;
	public String message;
	public int viewType;
	
	public long currentTime;//限时特价中使用的服务器当前时间
	public long endTime;//限时特价中的活动结束时间
}
