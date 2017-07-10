package com.jingdong.app.reader.bookshelf.animation;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 实现图片3D旋转动画
 * @author xuhongwei
 *
 */
public class Rotate3DAnimation extends Animation {  
    private final float mFromDegrees;  
    private final float mToDegrees;  
    private final float mCenterX;  
    private final float mCenterY;  
    private final float mDepthZ;  
    private final boolean mReverse;  
    private Camera mCamera;  
  
    public Rotate3DAnimation(float fromDegrees, float toDegrees, float centerX, float centerY, float depthZ,  
            boolean reverse) {  
        mFromDegrees = fromDegrees;  
        mToDegrees = toDegrees;  
        mCenterX = centerX;  
        mCenterY = centerY;  
        mDepthZ = depthZ;  
        mReverse = reverse;  
        
        mCamera = new Camera(); 
        if(0 != mDepthZ) {
        	mCamera.setLocation(0, 0, -mDepthZ);
        }
        
    }  
  
    @Override  
    public void initialize(int width, int height, int parentWidth, int parentHeight) {  
        super.initialize(width, height, parentWidth, parentHeight);  
    }  
  
    @Override  
    protected void applyTransformation(float interpolatedTime, Transformation t) {  
        final float fromDegrees = mFromDegrees;  
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);  
        final float centerX = mCenterX;  
        final float centerY = mCenterY;  
        final Camera camera = mCamera;  
        final Matrix matrix = t.getMatrix();  
        // 将当前的摄像头位置保存下来，以便变换进行完成后恢复成原位，  
        camera.save();  
        // camera.translate，这个方法接受3个参数，分别是x,y,z三个轴的偏移量，我们这里只将z轴进行了偏移，  
        if (mReverse) {  
            // z的偏移会越来越大。这就会形成这样一个效果，view从近到远  
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);  
        } else {  
            // z的偏移会越来越小。这就会形成这样一个效果，我们的View从一个很远的地方向我们移过来，越来越近，最终移到了我们的窗口上面～  
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));  
        }  
  
        // 是给我们的View加上旋转效果，在移动的过程中，视图还会移Y轴为中心进行旋转。  
        camera.rotateY(degrees); 
        // 是给我们的View加上旋转效果，在移动的过程中，视图还会移X轴为中心进行旋转。  
        // camera.rotateX(degrees);  
  
        // 这个是将我们刚才定义的一系列变换应用到变换矩阵上面，调用完这句之后，我们就可以将camera的位置恢复了，以便下一次再使用。  
        camera.getMatrix(matrix);  
        // camera位置恢复  
        camera.restore();  
  
        // 以View的中心点为旋转中心,如果不加这两句，就是以（0,0）点为旋转中心  
        matrix.preTranslate(-centerX, -centerY);  
        matrix.postTranslate(centerX, centerY);  
    }  
} 