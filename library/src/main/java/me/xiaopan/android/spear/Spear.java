/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.android.spear;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import me.xiaopan.android.spear.cache.disk.DiskCache;
import me.xiaopan.android.spear.cache.disk.LruDiskCache;
import me.xiaopan.android.spear.cache.memory.LruMemoryCache;
import me.xiaopan.android.spear.cache.memory.MemoryCache;
import me.xiaopan.android.spear.decode.DefaultImageDecoder;
import me.xiaopan.android.spear.decode.ImageDecoder;
import me.xiaopan.android.spear.download.ImageDownloader;
import me.xiaopan.android.spear.download.HttpClientImageDownloader;
import me.xiaopan.android.spear.execute.DefaultRequestExecutor;
import me.xiaopan.android.spear.execute.RequestExecutor;
import me.xiaopan.android.spear.request.DisplayRequest;
import me.xiaopan.android.spear.request.DownloadListener;
import me.xiaopan.android.spear.request.DownloadRequest;
import me.xiaopan.android.spear.request.LoadListener;
import me.xiaopan.android.spear.request.LoadRequest;
import me.xiaopan.android.spear.request.RequestOptions;
import me.xiaopan.android.spear.util.AsyncDrawable;
import me.xiaopan.android.spear.util.Scheme;

/**
 * 图片加载器，可以从网络或者本地加载图片，并且支持自动清除缓存
 */
public class Spear {
    public static final String LOG_TAG= Spear.class.getSimpleName();
	private static Spear instance;

    private Handler handler;	//消息处理器，用于在主线程显示图片
    private Context context;	//上下文

    private boolean debugMode;	//调试模式，在控制台输出日志
    private DiskCache diskCache;    // 磁盘缓存器
    private MemoryCache memoryCache;	//图片缓存器
    private ImageDecoder imageDecoder;	//图片解码器
    private ImageDownloader imageDownloader;	//图片下载器
    private RequestExecutor requestExecutor;	//请求执行器

	public Spear(Context context){
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        this.diskCache = new LruDiskCache(context);
        this.imageDownloader = new HttpClientImageDownloader();
        this.memoryCache = new LruMemoryCache();
        this.imageDecoder = new DefaultImageDecoder();
        this.requestExecutor = new DefaultRequestExecutor();
	}

    /**
     * 下载
     * @param uri 支持以下2种类型
     * <blockquote>“http://site.com/image.png“  // from Web
     * <br>“https://site.com/image.png“ // from Web
     * </blockquote>
     * @param downloadListener 下载监听器
     * @return DownloadRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始下载
     */
	public DownloadRequest.Builder download(String uri, DownloadListener downloadListener){
		 return new DownloadRequest.Builder(this, uri).listener(downloadListener);
	}



    /**
     * 加载
     * @param uri 支持以下6种类型
     * <blockquote>“http://site.com/image.png“  // from Web
     * <br>“https://site.com/image.png“ // from Web
     * <br>“file:///mnt/sdcard/image.png“ // from SD card
     * <br>“content://media/external/audio/albumart/13“ // from content provider
     * <br>“assets://image.png“ // from assets
     * <br>“drawable://" + R.drawable.image // from drawables
     * </blockquote>
     * @param loadListener 加载监听器
     * @return LoadRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始加载
     */
	public LoadRequest.Builder load(String uri, LoadListener loadListener){
        return new LoadRequest.Builder(this, uri).listener(loadListener);
	}
    
    /**
     * 加载
     * @param imageFile 图片文件
     * @param loadListener 加载监听器
     * @return LoadRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始加载
     */
	public LoadRequest.Builder load(File imageFile, LoadListener loadListener){
        return new LoadRequest.Builder(this, Scheme.FILE.createUri(imageFile.getPath())).listener(loadListener);
	}

    /**
     * 加载
     * @param drawableResId 图片资源ID
     * @param loadListener 加载监听器
     * @return LoadRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始加载
     */
	public LoadRequest.Builder load(int drawableResId, LoadListener loadListener){
        return new LoadRequest.Builder(this, Scheme.DRAWABLE.createUri(String.valueOf(drawableResId))).listener(loadListener);
	}

    /**
     * 加载
     * @param uri 图片资源URI
     * @param loadListener 加载监听器
     * @return LoadRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始加载
     */
	public LoadRequest.Builder load(Uri uri, LoadListener loadListener){
        return new LoadRequest.Builder(this, uri.toString()).listener(loadListener);
	}



    /**
     * 显示图片
     * @param uri 支持以下6种类型
     * <blockquote>“http://site.com/image.png“  // from Web
     * <br>“https://site.com/image.png“ // from Web
     * <br>“file:///mnt/sdcard/image.png“ // from SD card
     * <br>“content://media/external/audio/albumart/13“ // from content provider
     * <br>“assets://image.png“ // from assets
     * <br>“drawable://" + R.drawable.image // from drawables
     * </blockquote>
     * @param imageView 显示图片的视图
     * @return DisplayRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始显示
     */
    public DisplayRequest.Builder display(String uri, ImageView imageView){
        return new DisplayRequest.Builder(this, uri, imageView);
    }

    /**
     * 显示图片
     * @param imageFile 图片文件
     * @param imageView 显示图片的视图
     * @return DisplayRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始显示
     */
    public DisplayRequest.Builder display(File imageFile, ImageView imageView){
        return new DisplayRequest.Builder(this, Scheme.FILE.createUri(imageFile.getPath()), imageView);
    }

    /**
     * 显示图片
     * @param drawableResId 图片资源ID
     * @param imageView 显示图片的视图
     * @return DisplayRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始显示
     */
    public DisplayRequest.Builder display(int drawableResId, ImageView imageView){
        return new DisplayRequest.Builder(this, Scheme.DRAWABLE.createUri(String.valueOf(drawableResId)), imageView);
    }

    /**
     * 显示图片
     * @param uri 图片资源URI
     * @param imageView 显示图片的视图
     * @return DisplayRequest.Builder 你可以继续设置一些参数，最后调用fire()方法开始显示
     */
    public DisplayRequest.Builder display(Uri uri, ImageView imageView){
        return new DisplayRequest.Builder(this, uri.toString(), imageView);
    }
	
    /**
     * 清除内存缓存和磁盘缓存
     */
    public void clearAllCache() {
        clearMemoryCache();
        clearDiskCache();
    }

    /**
     * 清除内存缓存
     */
    public void clearMemoryCache() {
        if(memoryCache == null){
            return;
        }
        memoryCache.clear();
    }

    /**
     * 清除磁盘缓存
     */
    public void clearDiskCache() {
        if(diskCache == null){
            return;
        }
        diskCache.clear();
    }
    
    /**
     * 根据URI获取缓存文件
     */
    public File getCacheFileByUri(String uri){
        if(diskCache == null){
            return null;
        }
		return diskCache.getCacheFileByUri(uri);
    }

    /**
     * 获取上下文
     * @return 上下文
     */
    public Context getContext() {
        return context;
    }

    /**
     * 获取请求执行器
     * @return 请求执行器
     */
    public RequestExecutor getRequestExecutor() {
        return requestExecutor;
    }

    /**
     * 设置请求执行器
     * @param requestExecutor 请求执行器
     */
    public Spear setRequestExecutor(RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
        return this;
    }

    /**
     * 获取磁盘缓存器
     * @return 磁盘缓存器
     */
    public DiskCache getDiskCache() {
        return diskCache;
    }

    /**
     * 设置磁盘缓存器
     * @param diskCache 磁盘缓存器
     */
    public Spear setDiskCache(DiskCache diskCache) {
        this.diskCache = diskCache;
        return this;
    }

    /**
     * 获取内存缓存器
     * @return 内存缓存器
     */
    public MemoryCache getMemoryCache() {
        return memoryCache;
    }

    /**
     * 设置内存缓存器
     * @param memoryCache 内存缓存器
     */
    public Spear setMemoryCache(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    /**
     * 获取位图解码器
     * @return 位图解码器
     */
    public ImageDecoder getImageDecoder() {
        return imageDecoder;
    }

    /**
     * 设置位图解码器
     * @param imageDecoder 位图解码器
     */
    public Spear setImageDecoder(ImageDecoder imageDecoder) {
        this.imageDecoder = imageDecoder;
        return this;
    }

    /**
     * 获取消息处理器
     * @return 消息处理器，用来实现在主线程显示图片
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * 是否开启调试模式
     * @return 是否开启调试模式，开启调试模式后会在控制台输出LOG
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * 设置是否开启调试模式
     * @param debugMode 是否开启调试模式，开启调试模式后会在控制台输出LOG
     */
    public Spear setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    /**
     * 获取图片下载器
     */
    public ImageDownloader getImageDownloader() {
        return imageDownloader;
    }

    /**
     * 设置图片下载器
     * @param imageDownloader 图片下载器
     */
    public Spear setImageDownloader(ImageDownloader imageDownloader) {
        this.imageDownloader = imageDownloader;
        return this;
    }

    /**
     * 取消
     * @param imageView ImageView
     * @return true：当前ImageView有正在执行的任务并且取消成功；false：当前ImageView没有正在执行的任务
     */
    public static boolean cancel(ImageView imageView) {
        final DisplayRequest displayRequest = AsyncDrawable.getDisplayRequestByAsyncDrawable(imageView);
        if (displayRequest != null) {
            displayRequest.cancel();
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取选项
     * @param optionsName 选项名称
     * @return 选项
     */
    public static RequestOptions getOptions(Enum<?> optionsName){
        return OptionsMapInstanceHolder.OPTIONS_MAP.get(optionsName);
    }

    /**
     * 放入选项
     * @param optionsName 选项名称
     * @param options 选项
     */
    public static void putOptions(Enum<?> optionsName, RequestOptions options){
        OptionsMapInstanceHolder.OPTIONS_MAP.put(optionsName, options);
    }

    /**
     * 选项集合持有器
     */
    private static class OptionsMapInstanceHolder{
        private static final Map<Object, RequestOptions> OPTIONS_MAP = new HashMap<Object, RequestOptions>();
    }

    /**
     * 获取图片加载器的实例
     * @param context 用来初始化配置
     * @return 图片加载器的实例
     */
    public static Spear with(Context context){
        if(instance == null){
            synchronized (Spear.class){
                if(instance == null){
                    instance = new Spear(context);
                }
            }
        }
        return instance;
    }
}
