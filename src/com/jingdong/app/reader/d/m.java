/**
 * @author yfxiawei yfxiawei@360buy.com
 * com.jingdong.app.reader.d
 * m.java
 * 上午9:57:37
 */
package com.jingdong.app.reader.d;

/**
 * 历史原因倒是boot编译为d.m如果不是d.m则不兼容原始数据
 * 	private String d== bookid
 *	private String e== orderid
 *	public String a== certjosh
 *	public String b== contentjosh
 *	public String c== coverURl
 * @author yfxiawei yfxiawei@360buy.com 上午9:57:37
 * 
 */

import java.io.Serializable;

public class m implements Serializable {

	private static final long serialVersionUID = -2067522672179207836L;

	public String d;// bookid
	public String e;// orderid
	public String a;// certjosh
	public String b;// contentjosh
	public String c;// coverURl

}
