package com.jingdong.app.reader.epub.paging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.jingdong.app.reader.epub.css.CSS;

import android.graphics.Paint;
import android.support.v4.util.Pools.Pool;
import android.support.v4.util.Pools.SynchronizedPool;

public class PagePool {

    private static final int PAGE_POOL_SIZE = 500;
    
    private Pool<Page> pagePool;
    private Pool<PageLink> pageLinkPool;
    private Pool<PageLine> pageLinePool;
    private Pool<CSS> cssPool;
    private Pool<Block> blockPool;
    private Pool<Paint> paintPool;
    private Pool<ElementImage> elementImagePool;

    public PagePool(){
        pagePool = new SynchronizedPool<Page>(PAGE_POOL_SIZE);
        pageLinkPool = new SynchronizedPool<PageLink>(50);
        pageLinePool = new SynchronizedPool<PageLine>(PAGE_POOL_SIZE*2);
        elementImagePool = new SynchronizedPool<ElementImage>(200);
        cssPool = new SynchronizedPool<CSS>(PAGE_POOL_SIZE);
        blockPool = new SynchronizedPool<Block>(PAGE_POOL_SIZE);
        paintPool = new SynchronizedPool<Paint>(PAGE_POOL_SIZE);
    }
    
    protected Page acquirePage() {
        Page page = pagePool.acquire();
        if (page == null) {
            page = new Page();
        }
        return page;
    }
    
    protected void releasePage(Page page) {
        page.releaseContent();
        pagePool.release(page);
    }
    
    protected void asyncReleasePages(Collection<Page> pages) {
        new Thread(new ReleasePage(this, pages)).start();
    }
    
    protected PageLink acquirePageLink() {
        PageLink link = pageLinkPool.acquire();
        if (link == null) {
            link = new PageLink();
        }
        return link;
    }
    
    protected void releasePageLink(PageLink pageLink) {
        pageLink.release();
        pageLinkPool.release(pageLink);
    }
    
    protected void asyncReleasePageLinks(Collection<PageLink> pageLines) {
        new Thread(new ReleasePageLink(this, pageLines)).start();
    }
    
    protected PageLine acquirePageLine() {
        PageLine line = pageLinePool.acquire();
        if (line == null) {
            line = new PageLine();
        }
        return line;
    }
    
    protected void releasePageLine(PageLine pageLine) {
        pageLine.release();
        pageLinePool.release(pageLine);
    }
    
    protected void asyncReleasePageLines(Collection<PageLine> pageLines) {
        new Thread(new ReleasePageLine(this, pageLines)).start();
    }
    
    protected CSS acquireCSS() {
        CSS css = cssPool.acquire();
        if (css == null) {
            css = new CSS();
        }
        return css;
    }
    
    protected void releaseCSS(CSS css) {
        cssPool.release(css);
    }
    
    protected Block acquireBlock() {
        Block block = blockPool.acquire();
        if (block == null) {
            block = new Block();
        }
        return block;
    }
    
    protected void releaseBlock(Block block) {
        block.release();
        blockPool.release(block);
    }
    
    protected void asyncReleaseBlocks(Collection<Block> blocks) {
        new Thread(new ReleaseBlock(this, blocks)).start();
    }
    
    protected Paint acquirePaint() {
        Paint paint = paintPool.acquire();
        if (paint == null) {
            paint = new Paint();
        }
        return paint;
    }
    
    protected void releasePaint(Paint paint) {
        paintPool.release(paint);
    }
    
    protected ElementImage acquireElementImage() {
        ElementImage image = elementImagePool.acquire();
        if (image == null) {
            image = new ElementImage();
        }
        return image;
    }
    
    protected void releaseElementImage(ElementImage image) {
        image.initialize(null, null, null, null, 0);
        elementImagePool.release(image);
    }
    
    private class ReleasePage implements Runnable {
        List<Page> pageList = new ArrayList<Page>();
        PagePool pagePool;

        ReleasePage(PagePool pagePool, Collection<Page> collection) {
            this.pagePool = pagePool;
            pageList.addAll(collection);
        }

        @Override
        public void run() {
            Iterator<Page> iterator = pageList.iterator();
            while (iterator.hasNext()) {
                Page page = iterator.next();
                pagePool.releasePage(page);
            }
            pageList.clear();
        }

    }
    
    private class ReleasePageLink implements Runnable {
        List<PageLink> pageLinkList = new ArrayList<PageLink>();
        PagePool pagePool;

        ReleasePageLink(PagePool pagePool, Collection<PageLink> collection) {
            this.pagePool = pagePool;
            pageLinkList.addAll(collection);
        }

        @Override
        public void run() {
            Iterator<PageLink> iterator = pageLinkList.iterator();
            while (iterator.hasNext()) {
                PageLink pageLink = iterator.next();
                pagePool.releasePageLink(pageLink);
            }
            pageLinkList.clear();
        }

    }
    
    private class ReleasePageLine implements Runnable {
        List<PageLine> pageLineList = new ArrayList<PageLine>();
        PagePool pagePool;

        ReleasePageLine(PagePool pagePool, Collection<PageLine> collection) {
            this.pagePool = pagePool;
            pageLineList.addAll(collection);
        }

        @Override
        public void run() {
            Iterator<PageLine> iterator = pageLineList.iterator();
            while (iterator.hasNext()) {
                PageLine pageLine = iterator.next();
                pagePool.releasePageLine(pageLine);
            }
            pageLineList.clear();
        }

    }
    
    private class ReleaseBlock implements Runnable {
        List<Block> blocks = new ArrayList<Block>();
        PagePool pagePool;

        ReleaseBlock(PagePool pagePool, Collection<Block> collection) {
            this.pagePool = pagePool;
            blocks.addAll(collection);
        }

        @Override
        public void run() {
            Iterator<Block> iterator = blocks.iterator();
            while (iterator.hasNext()) {
                Block block = iterator.next();
                pagePool.releaseBlock(block);
            }
            blocks.clear();
        }

    }

}
