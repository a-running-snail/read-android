package com.jingdong.app.reader.epub.paging;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.epub.FilePath;
import com.jingdong.app.reader.epub.css.CSS;
import com.jingdong.app.reader.util.ImageUtils;
import com.jingdong.app.reader.util.MZLog;

public class ElementImage extends Element {

    private Size size;
    private Size pageSize;
    private Size parentSize;
    private String src;
    /**src+chapterItemRef用作LruCache的Key，为了避免src有重复情况*/
    private String chapterItemRef;
    private Map<String, String> attributeMap;
    private boolean isFootnote = false;
    private boolean isJumpAction = false;
    private boolean supportEnlarge = false;
    private boolean isPlayControl = false;
    private boolean isFloatRight = false;
    private boolean isFloatLeft = false;
    private boolean isInBlock = false;
    private boolean isEnlargeEnable = false;
    private boolean isDisplayHidden = false;
    private boolean isForcePageBreakAfter = false;
    private boolean isForcePageBreakBefore = false;
    private String footnoteId = null;
    private String audioLink;
    private ImageView view;
    private float pxPerEm;
    private float marginTop;
    private float marginLeft;
    private float marginRight;
    private float marginBottom;
    
    public ElementImage() {
        super(null);
    }

    public ElementImage(Map<String, String> attributeMap, Paint paint, Size pageSize) {
        super(paint);
        this.attributeMap = attributeMap;
        this.pageSize = pageSize;
        String classText = this.attributeMap.get("class");
        if (!TextUtils.isEmpty(classText)) {
            String href = this.attributeMap.get("href");
            if (!TextUtils.isEmpty(href)) {
                if (classText.equals("mz-footnote-link")) {
                    isFootnote = true;
                    footnoteId = href;
                } else if (classText.equals("mz-playaudio-control") || classText.equals("mz-audio-playcontrol")) {
                    isPlayControl = true;
                    audioLink = href;
                }
            }
        }
        String enlargeText = this.attributeMap.get("enlarge");
        if (!TextUtils.isEmpty(enlargeText) && enlargeText.equals("1")) {
            supportEnlarge = true;
        }
    }
    
    void initialize(Map<String, String> attributeMap, Paint paint, Size pageSize, Size parentSize, float pxPerEm) {
        this.paint = paint;
        if (this.attributeMap != null) {
            this.attributeMap.clear();
        }
        
        this.attributeMap = new HashMap<String, String>();
        if (attributeMap != null) {
            this.attributeMap.putAll(attributeMap);
        }
        this.pageSize = pageSize;
        this.parentSize = parentSize;
        if (pageSize != null && parentSize != null) {
            if (parentSize.width > pageSize.width || parentSize.height > pageSize.height) {
                float xScale = pageSize.width / parentSize.width;
                float yScale = pageSize.height / parentSize.height;
                
                float newWidth;
                float newHeight;
                
                if (xScale < yScale) {
                    newWidth = pageSize.width;
                    newHeight = parentSize.height * xScale;
                } else {
                    newWidth = parentSize.width * yScale;
                    newHeight = pageSize.height;
                }
                this.parentSize = new Size(newWidth, newHeight);
            }
        }
        if (this.view != null) {
            BookPageViewActivity.removeBitmapCache(src+chapterItemRef);
        }
        this.view = null;
        this.size = null;
        this.src = null;
        chapterItemRef = "";
        isFootnote = false;
        footnoteId = null;
        isJumpAction = false;
        supportEnlarge = false;
        isEnlargeEnable = false;
        isPlayControl = false;
        isInBlock = false;
        audioLink = null;
        this.rect = null;
        this.paraIndex = 0;
        this.offsetInPara = 0;
        this.setIsLink(false);
        this.setlinkUUID(null);
        this.setAnchorId(null);
        this.pxPerEm = pxPerEm;
        this.noteStatus = NoteStatus.UNNOTE;
        this.selectionStatus = SelectionStatus.UNSELECTION;
        if (this.attributeMap != null) {
            String classText = this.attributeMap.get("class");
            if (!TextUtils.isEmpty(classText)) {
                String href = this.attributeMap.get("href");
                if (!TextUtils.isEmpty(href)) {
                    if (classText.equals("mz-footnote-link")) {
                        isFootnote = true;
                        footnoteId = href;
                    } else if (classText.equals("mz-playaudio-control")
                            || classText.equals("mz-audio-playcontrol")) {
                        isPlayControl = true;
                        audioLink = href;
                    }
                }
            } else {
                String href = this.attributeMap.get("href");
                if (!TextUtils.isEmpty(href) && href.startsWith("#")) {
                    footnoteId = href;
                    isJumpAction = true;
                }
            }
            String id = this.attributeMap.get("id");
            if (!TextUtils.isEmpty(id)) {
                setAnchorId(id);
            }
            
            String enlargeText = this.attributeMap.get("enlarge");
            if (!TextUtils.isEmpty(enlargeText) && enlargeText.equals("1")) {
                supportEnlarge = true;
            }
            String displayText = this.attributeMap.get("display");
            if ("block".equalsIgnoreCase(displayText)) {
                this.isInBlock = true;
            } else {
                this.isInBlock = false;
            }
        }
        
        this.isFloatLeft = CSS.isFloatLeft(this.attributeMap);
        this.isFloatRight = CSS.isFloatRight(this.attributeMap);
        float[] margin = CSS.getMargin(this.attributeMap, this.pxPerEm);
        this.marginTop = margin!=null?margin[0]:CSS.getMarginTop(this.attributeMap, this.pxPerEm);
        this.marginRight = margin!=null?margin[1]:CSS.getMarginRight(this.attributeMap, this.pxPerEm);
        this.marginBottom = margin!=null?margin[2]:CSS.getMarginBottom(this.attributeMap, this.pxPerEm);
        this.marginLeft = margin!=null?margin[3]:CSS.getMarginLeft(this.attributeMap, this.pxPerEm);
        this.isForcePageBreakBefore = CSS.isForcePageBreakBefore(this.attributeMap);
        this.isForcePageBreakAfter = CSS.isForcePageBreakAfter(this.attributeMap);
        this.isDisplayHidden = CSS.isDisplayHidden(this.attributeMap);
    }

    public boolean supportEnlarge() {
        return supportEnlarge;
    }
    
    public void setSupportEnlarge(boolean enlarge) {
        supportEnlarge = enlarge;
    }
    
    public String getUrlText() {
        return attributeMap.get("href");
    }
    
    public ImageView getImageView() {
        return this.view;
    }
    
    public void setImageView(ImageView view) {
        this.view = view;
    }
    
    Bitmap getBitmap(float scale, Context context) {
        Bitmap bitmap = null;
        LruCache<String, Bitmap> bitmapCache = BookPageViewActivity.getBitmapCache();
        if (bitmapCache != null) {
            MZLog.d("ElementImage", "cache hitted");
            bitmap = bitmapCache.get(src+chapterItemRef);
        }
        if (bitmap == null) {
            MZLog.d("ElementImage", "cache missed");

            int width, height;
            if (isFullScreen()) {
                float reqWidth = pageSize.width + BookPageViewActivity.getPageMarginLeft() * scale
                        + BookPageViewActivity.getPageMarginRight() * scale;
                float reqHeight = pageSize.height + BookPageViewActivity.PageMarginTop * scale
                        + BookPageViewActivity.PageMarginBottom * scale;
                width = (int)reqWidth;
                height = (int)reqHeight;
            } else {
                width = (int)rect.width();
                height = (int)rect.height();
            }
            bitmap = ImageUtils.getBitmapFromNamePath(src, width, height);
            if (bitmapCache != null) {
                if (bitmap != null && src != null) {
                    bitmapCache.put(src+chapterItemRef, bitmap);
                }
            }
        }
        return bitmap;
    }
    
    ImageView.ScaleType getImageScaleType() {
        String mzScale = attributeMap.get("mz-scale");
        if ("fill".equalsIgnoreCase(mzScale)) {
            return ImageView.ScaleType.FIT_XY;
        } else if ("aspectfill".equalsIgnoreCase(mzScale)) {
            return ImageView.ScaleType.CENTER_CROP;
        }
        return null;
    }
    
    boolean isMZFullScreen() {
        String mzFullScreen = attributeMap.get("mz-fullscreen");
        if ("1".equals(mzFullScreen)) {
            return true;
        }
        return false;
    }

    boolean isFullScreen() {
        String mzFullScreen = attributeMap.get("mz-fullscreen");
        if (mzFullScreen != null) {
            return true;
        }

        String width = attributeMap.get("width");
        String height = attributeMap.get("height");
        if (width != null && height != null && width.equals("100%") && height.equals("100%")) {
            return true;
        }
        return false;
    }
    
    private int getMaxWidth() {
        float parentWidth = parentSize.width;
        if (parentWidth == 0) {
            parentWidth = pageSize.width;
        }
        int imageWidth = 0;
        String width = attributeMap.get("width");
        if (!TextUtils.isEmpty(width)) {
            try {
                if (width.endsWith("px") || width.endsWith("PX")
                        || width.endsWith("pX") || width.endsWith("Px")) {
                    width = width.substring(0, width.length() - 2);
                    float size = Float.parseFloat(width.trim());
                    imageWidth = (int) (size * BookPageViewActivity.getDensity());
                } else if (width.endsWith("em") || width.endsWith("EM")
                        || width.endsWith("eM") || width.endsWith("Em")) {
                    width = width.substring(0, width.length() - 2);
                    float size = Float.parseFloat(width.trim());
                    imageWidth = (int) (pxPerEm * size);
                } else if (width.endsWith("%")) {
                    width = width.substring(0, width.length() - 1);
                    float size = Float.parseFloat(width.trim());
                    imageWidth = (int) (parentWidth * size / 100);
                } else if (width.equalsIgnoreCase("auto")) {
                    imageWidth = (int) parentWidth;
                } else {
                    float size = Float.parseFloat(width.trim());
                    imageWidth = (int) (size * BookPageViewActivity.getDensity());
                }
            } catch (NumberFormatException e) {
            }
        }
        if (imageWidth > parentWidth) {
            return (int) parentWidth;
        } else if (imageWidth <= 0) {
            return (int) parentWidth;
        } else {
            return imageWidth;
        }
    }
    
    private int getMaxHeight() {
        float parentHeight = parentSize.height;
        if (parentHeight == 0) {
            parentHeight = pageSize.height;
        }
        int imageHeight = 0;
        String height = attributeMap.get("height");
        if (!TextUtils.isEmpty(height)) {
            try {
                if (height.endsWith("px") || height.endsWith("PX")
                        || height.endsWith("pX") || height.endsWith("Px")) {
                    height = height.substring(0, height.length() - 2);
                    float size = Float.parseFloat(height.trim());
                    imageHeight = (int) (size * BookPageViewActivity.getDensity());
                } else if (height.endsWith("em") || height.endsWith("EM")
                        || height.endsWith("eM") || height.endsWith("Em")) {
                    height = height.substring(0, height.length() - 2);
                    float size = Float.parseFloat(height.trim());
                    imageHeight = (int) (pxPerEm * size);
                } else if (height.endsWith("%")) {
                    height = height.substring(0, height.length() - 1);
                    float size = Float.parseFloat(height.trim());
                    imageHeight = (int) (parentHeight * size / 100);
                } else if (height.equalsIgnoreCase("auto")) {
                    imageHeight = (int) parentHeight;
                } else {
                    float size = Float.parseFloat(height.trim());
                    imageHeight = (int) (size * BookPageViewActivity.getDensity());
                }
            } catch (NumberFormatException e) {
            }
        }
        if (imageHeight > parentHeight) {
            return (int) parentHeight;
        } else if (imageHeight <= 0) {
            return (int) parentHeight;
        } else {
            return imageHeight;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Image: ");
        String src = attributeMap.get("src");
        if (src == null) {
            src = attributeMap.get("xlink:href");
        }
        sb.append(src);
        sb.append(']');
        return sb.toString();
    }
    
    @Override
    public
    String getContent() {
        return "[图]";
    }

    @Override
    int getCount() {
        return 1;
    }

    /**
     * 检测元素的大小
     */
    @Override
    Size measureSize(PageContext measurer, String basePath) {
        if (size != null) {
            return size;
        }

        if (isFootnote) {
            size = new Size(measurer.pxPerEm, measurer.pxPerEm);
            return size;
        }
        
        if (isPlayControl) {
            size = new Size(measurer.pxPerEm * 2f, measurer.pxPerEm);
            return size;
        }

        String relativePath = attributeMap.get("src");
        if (relativePath == null) {
            relativePath = attributeMap.get("xlink:href");
        }
        if (relativePath == null) {
            size = new Size(0, 0);
            return size;
        }

        try {
            src = URLDecoder.decode(FilePath.resolveRelativePath(basePath, relativePath), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (isFullScreen()) {
            size = new Size(pageSize.width, pageSize.height);
            return size;
        }
        
        float maxWidth = getMaxWidth();
        float maxHeight = getMaxHeight();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(src, options);

        float imageHeight = options.outHeight * BookPageViewActivity.getDensity();
        float imageWidth = options.outWidth * BookPageViewActivity.getDensity();
        
        if (isFloatLeft || isFloatRight) {
            if (imageWidth > pageSize.width / 2) {
                float temp = pageSize.width / 2;
                imageHeight = imageHeight * temp/imageWidth;
                imageWidth = temp;
            }
        }

        if (imageWidth > maxWidth || imageHeight > maxHeight) {
            // scale to fit screen
            float xScale = maxWidth / imageWidth;
            float yScale = maxHeight / imageHeight;

            float newWidth;
            float newHeight;

            if (xScale < yScale) {
                newWidth = maxWidth;
                newHeight = imageHeight * xScale;
            } else {
                newWidth = imageWidth * yScale;
                newHeight = maxHeight;
            }
            size = new Size(newWidth, newHeight);

        } else {

            size = new Size(imageWidth, imageHeight);
        }

        return size;
    }

    @Override
    void draw(Canvas canvas, PageContext measurer, Context context, float density, boolean isReadNoteShare) {
        if (isReadNoteShare) {
            Bitmap bitmap = getBitmap(density, context);

            // 获得图片的宽高
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            // 计算缩放比例
            float scaleWidth = size.width / width;
            float scaleHeight = size.height / height;
            if (this.isFullScreen()) {
                Matrix matrix = new Matrix();
                float scale = scaleWidth <= scaleHeight ? scaleWidth
                        : scaleHeight;
                matrix.postScale(scale, scale);
                // 得到新的图片
                Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                        matrix, true);
                // 放在画布上
                canvas.drawBitmap(newbm, 0, (size.height - height * scale) / 2,
                        paint);
                return;
            }
            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            if (scaleWidth >= scaleHeight) {
                matrix.postScale(scaleWidth, scaleWidth);
            } else {
                matrix.postScale(scaleHeight, scaleHeight);
            }
            // 得到新的图片
            Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                    matrix, true);
            // 放在画布上
            canvas.drawBitmap(newbm, rect.left, rect.top, paint);
        }
    }

    public String getSrc() {
        return this.src;
    }

    public boolean isFootnote() {
        return isFootnote;
    }

    public boolean isPlayControl() {
        return isPlayControl;
    }

    public boolean isFloatRight() {
        return isFloatRight;
    }

    public boolean isFloatLeft() {
        return isFloatLeft;
    }
    
    public void setFloatRight(boolean isFloatRight) {
        this.isFloatRight = isFloatRight;
    }

    public void setFloatLeft(boolean isFloatLeft) {
        this.isFloatLeft = isFloatLeft;
    }

    public void setParentFloat(int parentFloat) {
        if (parentFloat == 1 || parentFloat == 2) {
            if (this.isFloatLeft) {
                this.isFloatLeft = false;
            } else if (this.isFloatRight) {
                this.isFloatRight = false;
            }
        }
    }

    public String getFootnoteId() {
        return footnoteId;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public boolean isInBlock() {
        return isInBlock;
    }

    public void setInBlock(boolean isInBlock) {
        this.isInBlock = isInBlock;
    }

    public Size getParentSize() {
        return parentSize;
    }

    public boolean isJumpAction() {
        return isJumpAction;
    }

    public boolean isEnlargeEnable() {
        return isEnlargeEnable;
    }

    public void setEnlargeEnable(boolean isEnlargeEnable) {
        this.isEnlargeEnable = isEnlargeEnable;
    }

    public void setChapterItemRef(String chapterItemRef) {
        if (!TextUtils.isEmpty(chapterItemRef)) {
            this.chapterItemRef = chapterItemRef;
        }
    }

    public boolean isForcePageBreakAfter() {
        return isForcePageBreakAfter;
    }

    public boolean isForcePageBreakBefore() {
        return isForcePageBreakBefore;
    }

    public boolean isDisplayHidden() {
        return isDisplayHidden;
    }
    
    public void hideImage() {
        //FIXME table cell not support image
        attributeMap.put("src", "");
    }
}
