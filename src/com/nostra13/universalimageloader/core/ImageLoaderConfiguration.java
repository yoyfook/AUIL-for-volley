/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.core;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.utils.L;

import java.io.File;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @author Alexander Ustinov (me[at]rusfearuth[dot]su)
 * @since 1.0.1
 * @see com.nostra13.universalimageloader.cache.memory.MemoryCacheAware
 * @see com.nostra13.universalimageloader.cache.disc.DiscCacheAware
 * @see com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator
 */
public final class ImageLoaderConfiguration {
    public final int maxWidth;
    public final int maxHeight;
	public final CompressFormat imageCompressFormatForDiscCache;
	public final int imageQualityForDiscCache;
    public final MemoryCacheAware<String, Bitmap> memoryCache;
    public final DiscCacheAware discCache;
    public final FileNameGenerator fileNameGenerator;
    public final File discCacheDir;
    public static final int BUFFER_SIZE = 32 * 1024;


	private ImageLoaderConfiguration(final Builder builder) {
        maxWidth = builder.maxWidth;
        maxHeight = builder.maxHeight;
		imageCompressFormatForDiscCache = builder.imageCompressFormatForDiscCache;
		imageQualityForDiscCache = builder.imageQualityForDiscCache;
        memoryCache = builder.memoryCache;
        discCache = builder.discCache;
        discCacheDir = builder.discCacheDir;
        fileNameGenerator = builder.discCacheFileNameGenerator;
	}

	public static ImageLoaderConfiguration createDefault() {
		return new Builder().defaultBuilder().build();
	}

	/**
	 * Builder for {@link ImageLoaderConfiguration}
	 *
	 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
	 */
	public static class Builder {

		private static final String WARNING_OVERLAP_DISC_CACHE_PARAMS = "discCache(), discCacheSize() and discCacheFileCount calls overlap each other";
		private static final String WARNING_OVERLAP_DISC_CACHE_NAME_GENERATOR = "discCache() and discCacheFileNameGenerator() calls overlap each other";
		private static final String WARNING_OVERLAP_MEMORY_CACHE = "memoryCache() and memoryCacheSize() calls overlap each other";

        private int maxWidth = 0;
        private int maxHeight = 0;
		private CompressFormat imageCompressFormatForDiscCache = null;
		private int imageQualityForDiscCache = 0;

		private int memoryCacheSize = 0;
		private int discCacheSize = 0;
		private int discCacheFileCount = 0;

        private File discCacheDir = null;

		private MemoryCacheAware<String, Bitmap> memoryCache = null;
		private DiscCacheAware discCache = null;
		private FileNameGenerator discCacheFileNameGenerator = null;

        public Builder cacheImageSize(int maxWidth, int maxHeight) {
            if (maxWidth <= 0) throw new IllegalArgumentException("maxWith must be a positive number");
            if (maxHeight <= 0) throw new IllegalArgumentException("maxHeight must be a positive number");
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            return this;
        }


		/**
		 * Sets options for resizing/compressing of downloaded images before saving to disc cache.<br />
		 * <b>NOTE: Use this option only when you have appropriate needs. It can make ImageLoader slower.</b>
		 *
		 * @param compressFormat {@link android.graphics.Bitmap.CompressFormat Compress format} downloaded images to
		 *            save them at disc cache
		 * @param compressQuality Hint to the compressor, 0-100. 0 meaning compress for small size, 100 meaning compress
		 *            for max quality. Some formats, like PNG which is lossless, will ignore the quality setting
		 */
		public Builder discCacheExtraOptions(CompressFormat compressFormat, int compressQuality) {
			this.imageCompressFormatForDiscCache = compressFormat;
			this.imageQualityForDiscCache = compressQuality;
			return this;
		}

		/**
		 * Sets maximum memory cache size for {@link android.graphics.Bitmap bitmaps} (in bytes).<br />
		 * Default value - 1/8 of available app memory.<br />
		 * <b>NOTE:</b> If you use this method then
		 * {@link com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache LruMemoryCache} will be used as
		 * memory cache. You can use {@link #memoryCache(com.nostra13.universalimageloader.cache.memory.MemoryCacheAware)} method to set your own implementation of
		 * {@link com.nostra13.universalimageloader.cache.memory.MemoryCacheAware}.
		 */
		public Builder memoryCacheSize(int memoryCacheSize) {
			if (memoryCacheSize <= 0) throw new IllegalArgumentException("memoryCacheSize must be a positive number");

			if (memoryCache != null) {
				L.w(WARNING_OVERLAP_MEMORY_CACHE);
			}

			this.memoryCacheSize = memoryCacheSize;
			return this;
		}

		/**
		 * Sets maximum memory cache size (in percent of available app memory) for {@link android.graphics.Bitmap
		 * bitmaps}.<br />
		 * Default value - 1/8 of available app memory.<br />
		 * <b>NOTE:</b> If you use this method then
		 * {@link com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache LruMemoryCache} will be used as
		 * memory cache. You can use {@link #memoryCache(com.nostra13.universalimageloader.cache.memory.MemoryCacheAware)} method to set your own implementation of
		 * {@link com.nostra13.universalimageloader.cache.memory.MemoryCacheAware}.
		 */
		public Builder memoryCacheSizePercentage(int avaialbleMemoryPercent) {
			if (avaialbleMemoryPercent <= 0 || avaialbleMemoryPercent >= 100)
				throw new IllegalArgumentException("avaialbleMemoryPercent must be in range (0 < % < 100)");

			if (memoryCache != null) {
				L.w(WARNING_OVERLAP_MEMORY_CACHE);
			}

			long availableMemory = Runtime.getRuntime().maxMemory();
			memoryCacheSize = (int) (availableMemory * (avaialbleMemoryPercent / 100f));
			return this;
		}

		/**
		 * Sets memory cache for {@link android.graphics.Bitmap bitmaps}.<br />
		 * Default value - {@link com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache LruMemoryCache}
		 * with limited memory cache size (size = 1/8 of available app memory)<br />
		 * <br />
		 * <b>NOTE:</b> If you set custom memory cache then following configuration option will not be considered:
		 * <ul>
		 * <li>{@link #memoryCacheSize(int)}</li>
		 * </ul>
		 */
		public Builder memoryCache(MemoryCacheAware<String, Bitmap> memoryCache) {
			if (memoryCacheSize != 0) {
				L.w(WARNING_OVERLAP_MEMORY_CACHE);
			}

			this.memoryCache = memoryCache;
			return this;
		}

		/**
		 * Sets maximum disc cache size for images (in bytes).<br />
		 * By default: disc cache is unlimited.<br />
		 * <b>NOTE:</b> If you use this method then
		 * {@link com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache TotalSizeLimitedDiscCache}
		 * will be used as disc cache. You can use {@link #discCache(com.nostra13.universalimageloader.cache.disc.DiscCacheAware)} method for introduction your own
		 * implementation of {@link com.nostra13.universalimageloader.cache.disc.DiscCacheAware}
		 */
		public Builder discCacheSize(int maxCacheSize) {
			if (maxCacheSize <= 0) throw new IllegalArgumentException("maxCacheSize must be a positive number");

			if (discCache != null || discCacheFileCount > 0) {
				L.w(WARNING_OVERLAP_DISC_CACHE_PARAMS);
			}

			this.discCacheSize = maxCacheSize;
			return this;
		}

		/**
		 * Sets maximum file count in disc cache directory.<br />
		 * By default: disc cache is unlimited.<br />
		 * <b>NOTE:</b> If you use this method then
		 * {@link com.nostra13.universalimageloader.cache.disc.impl.FileCountLimitedDiscCache FileCountLimitedDiscCache}
		 * will be used as disc cache. You can use {@link #discCache(com.nostra13.universalimageloader.cache.disc.DiscCacheAware)} method for introduction your own
		 * implementation of {@link com.nostra13.universalimageloader.cache.disc.DiscCacheAware}
		 */
		public Builder discCacheFileCount(int maxFileCount) {
			if (maxFileCount <= 0) throw new IllegalArgumentException("maxFileCount must be a positive number");

			if (discCache != null || discCacheSize > 0) {
				L.w(WARNING_OVERLAP_DISC_CACHE_PARAMS);
			}

			this.discCacheSize = 0;
			this.discCacheFileCount = maxFileCount;
			return this;
		}

		/**
		 * Sets name generator for files cached in disc cache.<br />
		 * Default value -
		 * {@link com.nostra13.universalimageloader.core.DefaultConfigurationFactory#createFileNameGenerator()
		 * DefaultConfigurationFactory.createFileNameGenerator()}
		 */
		public Builder discCacheFileNameGenerator(FileNameGenerator fileNameGenerator) {
			if (discCache != null) {
				L.w(WARNING_OVERLAP_DISC_CACHE_NAME_GENERATOR);
			}

			this.discCacheFileNameGenerator = fileNameGenerator;
			return this;
		}

        public Builder discCacheDir(File cacheDir) {
            if (discCacheSize > 0 || discCacheFileCount > 0) {
                L.w(WARNING_OVERLAP_DISC_CACHE_PARAMS);
            }
            if (discCacheFileNameGenerator != null) {
                L.w(WARNING_OVERLAP_DISC_CACHE_NAME_GENERATOR);
            }

            this.discCacheDir = cacheDir;

            return this;
        }

		/**
		 * Sets disc cache for images.<br />
		 * Default value - {@link com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache
		 * UnlimitedDiscCache}. Cache directory is defined by
		 * {@link com.nostra13.universalimageloader.utils.StorageUtils#getCacheDirectory(android.content.Context)
		 * StorageUtils.getCacheDirectory(Context)}.<br />
		 * <br />
		 * <b>NOTE:</b> If you set custom disc cache then following configuration option will not be considered:
		 * <ul>
		 * <li>{@link #discCacheSize(int)}</li>
		 * <li>{@link #discCacheFileCount(int)}</li>
		 * <li>{@link #discCacheFileNameGenerator(com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator)}</li>
		 * </ul>
		 */
		public Builder discCache(DiscCacheAware discCache) {
			if (discCacheSize > 0 || discCacheFileCount > 0) {
				L.w(WARNING_OVERLAP_DISC_CACHE_PARAMS);
			}
			if (discCacheFileNameGenerator != null) {
				L.w(WARNING_OVERLAP_DISC_CACHE_NAME_GENERATOR);
			}

			this.discCache = discCache;
			return this;
		}

        public Builder defaultBuilder() {

            cacheImageSize(480, 800);
            memoryCache(new LRULimitedMemoryCache(8 * 1024 * 1024)); // 5 Mbs
            memoryCacheSize(5 * 1024 * 1024); // 5 Mbs

            return this;
        }

		/** Builds configured {@link ImageLoaderConfiguration} object */
		public ImageLoaderConfiguration build() {
			return new ImageLoaderConfiguration(this);
		}
	}
}
