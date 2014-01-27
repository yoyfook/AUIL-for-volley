/*******************************************************************************
 * Copyright 2013 Sergey Tarasevich
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
package com.nostra13.universalimageloader.utils;

import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.lang.reflect.Field;

/**
 * Provides calculations with image sizes, scales
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.8.3
 */
public final class ImageSizeUtils {

	private ImageSizeUtils() {
	}

	/**
	 * Defines target size for image. Size is defined by target {@link android.widget.ImageView view} parameters, configuration
	 * parameters or device display dimensions.<br />
	 * Size computing algorithm:<br />
	 * 1) Get the actual drawn <b>getWidth()</b> and <b>getHeight()</b> of the View. If view haven't drawn yet then go
	 * to step #2.<br />
	 * 2) Get <b>layout_width</b> and <b>layout_height</b>. If both of them haven't exact value then go to step #3.<br />
	 * 3) Get <b>maxWidth</b> and <b>maxHeight</b>. If both of them are not set then go to step #4.<br />
	 * 4) Get <b>maxImageWidth</b> param (<b>maxImageWidthForMemoryCache</b>) and <b>maxImageHeight</b> param
	 * (<b>maxImageHeightForMemoryCache</b>). If both of them are not set (equal 0) then go to step #5.<br />
	 * 5) Get device screen dimensions.
	 */
	public static ImageSize defineTargetSizeForView(ImageView imageView, int maxImageWidth, int maxImageHeight) {
		final DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

		final LayoutParams params = imageView.getLayoutParams();
		int width = (params != null && params.width == LayoutParams.WRAP_CONTENT) ? 0 : imageView.getWidth(); // Get actual image width
		if (width <= 0 && params != null) width = params.width; // Get layout width parameter
		if (width <= 0) width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check maxWidth parameter
		if (width <= 0) width = maxImageWidth;
		if (width <= 0) width = displayMetrics.widthPixels;

		int height = (params != null && params.height == LayoutParams.WRAP_CONTENT) ? 0 : imageView.getHeight(); // Get actual image height
		if (height <= 0 && params != null) height = params.height; // Get layout height parameter
		if (height <= 0) height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check maxHeight parameter
		if (height <= 0) height = maxImageHeight;
		if (height <= 0) height = displayMetrics.heightPixels;

		return new ImageSize(width, height);
	}

	private static int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = (Integer) field.get(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;
			}
		} catch (Exception e) {
			L.e(e);
		}
		return value;
	}
}
