
package com.jingdong.app.reader.util;

import android.content.Context;
import android.os.Build;

public final class DeviceUtil {

    private DeviceUtil() {
    }

    public static String getBranchAndModel(){
        return Build.BRAND+"/"+Build.MODEL;
    }
    
    public static String getBranch(){
        return Build.BRAND;
    }
    
    public static String getModel(){
        return Build.MODEL;
    }

    /**
     * 获取到移动设备的IMEI号, <br>
     * 2011-1-5 刘卫欢 增加
     *
     * @param context
     * @return
     */
    public static String getDeviceImei(Context context) {
        String  imei = CommonUtil.getDeviceId();;
//        TelephonyManager tm = (TelephonyManager) context
//                .getSystemService(Context.TELEPHONY_SERVICE);
//        if (tm != null) {
//            imei = tm.getDeviceId();
//        }
        return imei;
    }

}
