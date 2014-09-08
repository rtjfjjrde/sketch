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

package me.xiaopan.android.spear.execute;

import android.util.Log;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import me.xiaopan.android.spear.Spear;
import me.xiaopan.android.spear.decode.AssetsDecodeListener;
import me.xiaopan.android.spear.decode.CacheFileDecodeListener;
import me.xiaopan.android.spear.decode.ContentDecodeListener;
import me.xiaopan.android.spear.decode.DrawableDecodeListener;
import me.xiaopan.android.spear.decode.FileDecodeListener;
import me.xiaopan.android.spear.request.DisplayRequest;
import me.xiaopan.android.spear.request.DownloadRequest;
import me.xiaopan.android.spear.request.LoadRequest;
import me.xiaopan.android.spear.request.Request;
import me.xiaopan.android.spear.task.DisplayJoinLoadListener;
import me.xiaopan.android.spear.task.DisplayJoinLoadProgressCallback;
import me.xiaopan.android.spear.task.DownloadTask;
import me.xiaopan.android.spear.task.LoadJoinDownloadListener;
import me.xiaopan.android.spear.task.LoadJoinDownloadProgressCallback;
import me.xiaopan.android.spear.task.LoadTask;
import me.xiaopan.android.spear.util.Scheme;

/**
 * 默认的请求执行器
 */
public class DefaultRequestExecutor implements RequestExecutor {
	private static final String NAME= DefaultRequestExecutor.class.getSimpleName();
	private Executor taskDispatchExecutor;	//任务调度执行器
	private Executor netTaskExecutor;	//网络任务执行器
	private Executor localTaskExecutor;	//本地任务执行器
	
	public DefaultRequestExecutor(Executor taskDispatchExecutor, Executor netTaskExecutor, Executor localTaskExecutor){
		this.taskDispatchExecutor = taskDispatchExecutor;
		this.netTaskExecutor = netTaskExecutor;
		this.localTaskExecutor = localTaskExecutor;
	}
	
	public DefaultRequestExecutor(Executor netTaskExecutor){
		this(
			new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20), new ThreadPoolExecutor.DiscardOldestPolicy()),
			netTaskExecutor, 
			new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20), new ThreadPoolExecutor.DiscardOldestPolicy())
		);
	}
	
	public DefaultRequestExecutor(){
		this(new ThreadPoolExecutor(5, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(20), new ThreadPoolExecutor.DiscardOldestPolicy()));
	}
	
	@Override
	public void execute(final Request request) {
		taskDispatchExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (request instanceof DisplayRequest) {
                    executeDisplayRequest((DisplayRequest) request);
                } else if (request instanceof LoadRequest) {
                    executeLoadRequest((LoadRequest) request);
                } else if (request instanceof DownloadRequest) {
                    executeDownloadRequest((DownloadRequest) request);
                }
            }
        });
	}
	
	/**
	 * 执行下载请求
	 * @param downloadRequest 下载请求
	 */
	private void executeDownloadRequest(DownloadRequest downloadRequest){
		File cacheFile = downloadRequest.getSpear().getDiskCache().createFile(downloadRequest);
		downloadRequest.setCacheFile(cacheFile);
		if(cacheFile != null && cacheFile.exists()){
			localTaskExecutor.execute(new DownloadTask(downloadRequest));
			if(downloadRequest.getSpear().isDebugMode()){
				Log.d(Spear.LOG_TAG, NAME + "：" + "DOWNLOAD - 本地" + "；" + downloadRequest.getName());
			}
		}else{
			netTaskExecutor.execute(new DownloadTask(downloadRequest));
			if(downloadRequest.getSpear().isDebugMode()){
				Log.d(Spear.LOG_TAG, NAME + "：" + "DOWNLOAD - 网络" + "；" + downloadRequest.getName());
			}
		}
	}
	
	/**
	 * 执行加载请求
	 * @param loadRequest 记载请求
	 */
	private void executeLoadRequest(LoadRequest loadRequest){
		switch(loadRequest.getScheme()){
			case HTTP :
			case HTTPS : 
				File cacheFile = loadRequest.getSpear().getDiskCache().createFile(loadRequest);
                loadRequest.setCacheFile(cacheFile);
                if(cacheFile != null && cacheFile.exists()){
                	if(!loadRequest.getSpear().getImageDownloader().isDownloadingByCacheFilePath(cacheFile.getPath())){
                		localTaskExecutor.execute(new LoadTask(loadRequest, new CacheFileDecodeListener(cacheFile, loadRequest)));
                		if(loadRequest.getSpear().isDebugMode()){
                			Log.d(Spear.LOG_TAG, NAME + "：" + "LOAD - HTTP - 本地" + "；" + loadRequest.getName());
                		}
                	}else{
                        loadRequest.setDownloadListener(new LoadJoinDownloadListener(localTaskExecutor, loadRequest));
                        if(loadRequest.getLoadProgressCallback() != null){
                            loadRequest.setDownloadProgressCallback(new LoadJoinDownloadProgressCallback(loadRequest.getLoadProgressCallback()));
                        }
                		netTaskExecutor.execute(new DownloadTask(loadRequest));
                        if(loadRequest.getSpear().isDebugMode()){
                            Log.d(Spear.LOG_TAG, NAME + "：" + "LOAD - HTTP - 网络 - 正在下载" + "；" + loadRequest.getName());
                        }
                	}
                }else{
                    loadRequest.setDownloadListener(new LoadJoinDownloadListener(localTaskExecutor, loadRequest));
                    if(loadRequest.getLoadProgressCallback() != null){
                        loadRequest.setDownloadProgressCallback(new LoadJoinDownloadProgressCallback(loadRequest.getLoadProgressCallback()));
                    }
                    netTaskExecutor.execute(new DownloadTask(loadRequest));
                    if(loadRequest.getSpear().isDebugMode()){
                        Log.d(Spear.LOG_TAG, NAME + "：" + "LOAD - HTTP - 网络" + "；" + loadRequest.getName());
                    }
                }
				break;
			case FILE :
                localTaskExecutor.execute(new LoadTask(loadRequest, new FileDecodeListener(new File(Scheme.FILE.crop(loadRequest.getUri())), loadRequest)));
                if(loadRequest.getSpear().isDebugMode()){
                    Log.d(Spear.LOG_TAG, NAME + "：" + "LOAD - FILE" + "；" + loadRequest.getName());
                }
				break;
			case ASSETS :
                localTaskExecutor.execute(new LoadTask(loadRequest, new AssetsDecodeListener(Scheme.ASSETS.crop(loadRequest.getUri()), loadRequest)));
                if(loadRequest.getSpear().isDebugMode()){
                    Log.d(Spear.LOG_TAG, NAME + "：" + "LOAD - ASSETS" + "；" + loadRequest.getName());
                }
				break;
			case CONTENT :
                localTaskExecutor.execute(new LoadTask(loadRequest, new ContentDecodeListener(loadRequest.getUri(), loadRequest)));
                if(loadRequest.getSpear().isDebugMode()){
                    Log.d(Spear.LOG_TAG, NAME + "：" + "LOAD - CONTENT" + "；" + loadRequest.getName());
                }
				break;
			case DRAWABLE :
                localTaskExecutor.execute(new LoadTask(loadRequest, new DrawableDecodeListener(Scheme.DRAWABLE.crop(loadRequest.getUri()), loadRequest)));
                if(loadRequest.getSpear().isDebugMode()){
                    Log.d(Spear.LOG_TAG, NAME + "：" + "LOAD - DRAWABLE" + "；" + loadRequest.getName());
                }
                break;
			default:
                if(loadRequest.getSpear().isDebugMode()){
                    Log.e(Spear.LOG_TAG, NAME + "：" + "LOAD - 未知的协议格式" + "：" + loadRequest.getUri());
                }
				break;
		}
	}

    /**
     * 执行显示请求
     * @param displayRequest 显示请求
     */
    private void executeDisplayRequest(DisplayRequest displayRequest){
        displayRequest.setLoadListener(new DisplayJoinLoadListener(displayRequest));
        if(displayRequest.getDisplayProgressCallback() != null){
            displayRequest.setLoadProgressCallback(new DisplayJoinLoadProgressCallback(displayRequest, displayRequest.getDisplayProgressCallback()));
        }
        executeLoadRequest(displayRequest);
    }
}
