/*
 Copyright (c) 2000 - 2004, The JAP-Team
 All rights reserved.
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution.

 - Neither the name of the University of Technology Dresden, Germany nor the names of its contributors
  may be used to endorse or promote products derived from this software without specific
  prior written permission.


 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS
 OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */
package gui;

import java.util.Hashtable;
import java.net.URL;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import javax.swing.ImageIcon;

import anon.util.ResourceLoader;
import logging.LogHolder;
import logging.LogLevel;
import logging.LogType;

/**
 * This class loads resources from the file system.
 */
final public class ImageIconLoader
{
	/**
	 * The default path to store images.
	 */
	public static final String DEFAULT_IMGAGE_PATH = "images/";
	/**
	 * Images with a smaller pixes size than 16 bit should be stored in this path. Their names must
	 * be equal to the corresponding images in the default path.
	 */
	public static final String DEFAULT_IMGAGE_PATH_LOWCOLOR = "images/lowcolor/";

	// all loaded icons are stored in the cache and do not need to be reloaded from file
	private static Hashtable ms_iconCache = new Hashtable();

	private ImageIconLoader()
	{
	}

	/**
	 * Loads an ImageIcon from the classpath or the current directory.
	 * The icon may be contained in an archive (JAR) or a directory structure. If the icon could
	 * not be found in the classpath, it is loaded from the current directory.
	 * If even the current directory does not contain the icon, it is loaded from the default image path.
	 * Once an icon is loaded, it is stored in a memory cache, so that further calls of this method
	 * do not load the icon from the file system, but from the cache.
	 * @param a_strRelativeImagePath the relative resource path or filename of the Image
	 * @return the loaded ImageIcon or null if the icon could not be loaded
	 *         (getImageLoadStatus() == java.awt.MediaTracker.ERRORED)
	 */
	public static ImageIcon loadImageIcon(String a_strRelativeImagePath)
	{
		return loadImageIcon(a_strRelativeImagePath, true);
	}

	/**
	 * Loads an ImageIcon from the classpath or the current directory.
	 * The icon may be contained in an archive (JAR) or a directory structure. If the icon could
	 * not be found in the classpath, it is loaded from the current directory.
	 * If even the current directory does not contain the icon, it is loaded from the default image path.
	 * Once an icon is loaded, it is stored in a memory cache, so that further calls of this method
	 * do not load the icon from the file system, but from the cache.
	 * The image may be loaded synchronously so that the method only returns when the image has been
	 * loaded completely (or an error occured), or asynchronously so that the method returns even if
	 * the image has not been loaded yet.
	 * @param a_strRelativeImagePath the relative resource path or filename of the Image
	 * @param a_bSync true if the image is loaded synchronously; false otherwise
	 * @return the loaded ImageIcon or null if the icon could not be loaded
	 *         (getImageLoadStatus() == java.awt.MediaTracker.ERRORED)
	 */
	public static ImageIcon loadImageIcon(String a_strRelativeImagePath, boolean a_bSync)
	{
		ImageIcon img;
		int statusBits;

		// try to load the image from the cache
		if (ms_iconCache.containsKey(a_strRelativeImagePath))
		{
			return new ImageIcon((Image)ms_iconCache.get(a_strRelativeImagePath));
		}

		// load image from the local classpath or the local directory
		img = loadImageIconInternal(ResourceLoader.getResourceURL(a_strRelativeImagePath));

		if (img == null && (Toolkit.getDefaultToolkit().getColorModel().getPixelSize() <= 16))
		{
			// load the image from the low color image path
			img = loadImageIconInternal(
				 ResourceLoader.getResourceURL(DEFAULT_IMGAGE_PATH_LOWCOLOR + a_strRelativeImagePath));
		}

		if (img == null || img.getImageLoadStatus() == MediaTracker.ERRORED)
		{
			// load the image from the default image path
			img = loadImageIconInternal(
						 ResourceLoader.getResourceURL(DEFAULT_IMGAGE_PATH + a_strRelativeImagePath));
		}

		if (img != null)
		{
			if (a_bSync)
			{
				statusBits = MediaTracker.ABORTED | MediaTracker.ERRORED | MediaTracker.COMPLETE;
				while ( (img.getImageLoadStatus() & statusBits) == 0)
				{
					Thread.yield();
				}
			}

			// write the image to the cache
			ms_iconCache.put(a_strRelativeImagePath, img.getImage());
		}

		statusBits = MediaTracker.ABORTED | MediaTracker.ERRORED;
		if (img == null || (img.getImageLoadStatus() & statusBits) != 0)
		{
			LogHolder.log(LogLevel.INFO, LogType.GUI,
						  "Could not load requested image '" + a_strRelativeImagePath + "'!");
		}

		return img;
	}

	private static ImageIcon loadImageIconInternal(URL a_imageURL)
	{
		try
		{
			return new ImageIcon(a_imageURL);
		}
		catch (NullPointerException a_e)
		{
			return null;
		}
	}
}
