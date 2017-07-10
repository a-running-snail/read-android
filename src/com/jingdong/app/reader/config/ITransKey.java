package com.jingdong.app.reader.config;

/**
 * 常量接口，定义中转页面所需要传输数据的存储Key 存放从发布Tab页面，选择各种大类别、小类别，地域、商铺等信息后，
 * 生成一个目标Vector，该数据结构对应的存储Key（存入DataCore）,
 * 以及其他普通String的必备参数的Key（压入Bundle，通过Intent传输过去模板Activity）。
 * 
 * @author zhoubo
 */
public interface ITransKey {

	static final String KEY = "key";

	static final String KEY1 = "key1";

	static final String KEY2 = "key2";

	static final String KEY3 = "key3";

	static final String KEY4 = "key4";

	static final String KEY5 = "key5";

	static final String KEY_OBJ = "key_object";

	static final String KEY_TAB_ID = "main_tab_id";

	static final String KEY_TAB_SUB_ID = "main_tab_sub_id";

	static final String KEY_IS_READ_DB = "isReadDB";

	static final String KEY_COOKIES_SAVE = "saveCookies";

	static final String KEY_USERPIN = "userPin";

	static final String KEY_ENCRYPTRESULT = "encryptResult";

	static final String KEY_WORD = "keyWord";

	static final String KEY_PAGEINDEX = "pageindex";

	static final String ACTIVITY_TYPE = "ActivityType";

	static final String FILTERSTR = "filterStr";

	static final String SUBCATAGROYNAME = "subCatagroyName";

	/**
	 * Intent传输的数据标识KEY，代表城市
	 */
	final String EXTRA_KEY_CITY = "city";

	/**
	 * Intent传输的数据标识KEY，代表城市显示名称
	 */
	final String EXTRA_KEY_CITYNAME = "cityName";

	/**
	 * Intent传输的数据标识KEY，代表城市 Composite Script Index
	 */
	final String EXTRA_KEY_CITYCOMPOSITESCRIPTINDEX = "city_composite_script_index";

	/**
	 * Intent传输的数据标识KEY，代表地区ID
	 */
	final String EXTRA_KEY_DISTRICTID = "district_id";

	/**
	 * Intent传输的数据标识KEY，代表街道ID
	 */
	final String EXTRA_KEY_STREETID = "street_id";

	/**
	 * Intent传输的数据标识KEY，代表大分类的ID
	 */
	final String EXTRA_KEY_CATEGORYID = "category_id";

	/**
	 * Intent传输的数据标识KEY，代表大分类的Name
	 */
	final String EXTRA_KEY_CATEGORYNAME = "category_name";

	/**
	 * Intent传输的数据标识KEY，代表小分类的ID
	 */
	final String EXTRA_KEY_MAJORCATEGORY = "major_cateogry";

	/**
	 * Intent传输的数据标识KEY，代表小分类的Name
	 */
	final String EXTRA_KEY_MAJORCATEGORYNAME = "major_cateogry_name";

	/**
	 * Intent传输的数据标识KEY，代表小分类的 Script Index
	 */
	final String EXTRA_KEY_MAJORCATEGORYSCRIPTINDEX = "major_category_script_index";

	/**
	 * Intent传输的数据标识KEY，代表 求职简历 兼职、全职 URL
	 */
	final String EXTRA_KEY_FINDJOB_FULLTIME_URL = "findjob_fulltime_url";

	/**
	 * Intent传输的数据标识KEY，从DataCore中取出预设值数据(来自于网络)
	 */
	final String EXTRA_KEY_TEMPLATE_PERSETDATA = "persetDataKey";

	/**
	 * Intent传输的数据标识KEY，代表每次点击"立即发布"发帖按钮时，动态生成的UUID
	 */
	final String EXTRA_KEY_SEQID = "SeqID";

	/**
	 * 加入发帖照片时候，获取Photo List 的Key <br/>
	 * <br/>
	 * Data类型:ArrayList<Uri>
	 */
	final String EXTRA_PHOTO_URI_LIST = "photoUrisList";

	/**
	 * 加入发帖照片时候，传入当前已经有多少张照片的Key <br/>
	 * <br/>
	 * Data类型:int
	 */
	final String EXTRA_PHOTO_COUNT = "photoCount";

	/**
	 * 加入发帖照片时候，传入当前还能拍多少张照片的Key <br/>
	 * <br/>
	 * Data类型:int
	 */
	final String EXTRA_PHOTO_REMAIN = "photoRemain";

	/**
	 * 加入发帖照片时候，Photo存储路径 的Key <br/>
	 * <br/>
	 * Data类型:String或Uri
	 */
	final String EXTRA_PHOTO_SAVE_PATH = "photoSavePath";

	/**
	 * 加入发帖照片时候，是否支持单拍/连拍功能 <br/>
	 * <br/>
	 * Data类型:boolean
	 */
	final String EXTRA_PHOTO_SINGLE = "photoSingel";

	/**
	 * 从资料页进入注册引导页的标识 </br> Data类型:boolean
	 */
	final String EXTRA_ORAIGIN_UPDATE_DETAIL = "pageOrigin";

	/**
	 * 从注册引导页返回资料页的标识，是否完成激活操作:true完成;false未完成 </br> Data类型:boolean
	 */
	final String EXTRA_IS_REG_SUCCEED = "isRegSucceed";

}
