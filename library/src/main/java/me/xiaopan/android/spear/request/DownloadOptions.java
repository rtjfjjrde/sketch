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

package me.xiaopan.android.spear.request;

/**
 * 下载选项
 */
public class DownloadOptions implements RequestOptions {
    private long diskCachePeriodOfValidity;	//磁盘缓存有效期，单位毫秒
    private boolean enableDiskCache = true;	//是否开启磁盘缓存

    /**
     * 开启硬盘缓存
     * @param enableDiskCache 开启磁盘缓存
     * @return DownloadOptions
     */
    public DownloadOptions enableDiskCache(boolean enableDiskCache) {
        this.enableDiskCache = enableDiskCache;
        return this;
    }

    /**
     * 设置硬盘缓存有效期
     * @param diskCachePeriodOfValidity 硬盘缓存有效期，单位毫秒，小于等于0表示永不过期
     * @return DownloadOptions
     */
    public DownloadOptions diskCachePeriodOfValidity(long diskCachePeriodOfValidity) {
        this.diskCachePeriodOfValidity = diskCachePeriodOfValidity;
        return this;
    }

    /**
     * 获取磁盘缓存有效期
     * @return 磁盘缓存有效期
     */
    public long getDiskCachePeriodOfValidity() {
        return diskCachePeriodOfValidity;
    }

    /**
     * 是否开启磁盘缓存
     * @return 是否开启磁盘缓存
     */
    public boolean isEnableDiskCache() {
        return enableDiskCache;
    }
}
