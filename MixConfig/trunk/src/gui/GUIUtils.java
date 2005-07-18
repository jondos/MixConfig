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

import java.awt.Point;
import java.awt.Dimension;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.ImageIcon;

/**
 * This class contains helper methods for the GUI.
 */
public final class GUIUtils
{
	public static final String IMGPATHHICOLOR = "images/";
	public static final String IMGPATHLOWCOLOR = "images/lowcolor/";

	/**
	 * Loads an Image from a File or a Resource.
	 * @param strImage the Resource or filename of the Image
	 * @param sync true if the loading is synchron, false if it should be asynchron
	 * @return the loaded image or an empty icon if the image could not be loaded
	 */
	public static ImageIcon loadImageIcon(String strImage, boolean sync)
	{
		ImageIcon img = null;

		// try loading the lowcolor images
		if (Toolkit.getDefaultToolkit().getColorModel().getPixelSize() <= 16)
		{
			img = ImageIconLoader.loadImageIcon(IMGPATHLOWCOLOR + strImage, sync);
		}
		// if loading of lowcolor images was not successful or
		//    we have to load the hicolor images
		if (img == null || img.getImageLoadStatus() == MediaTracker.ERRORED)
		{
			img = ImageIconLoader.loadImageIcon(IMGPATHHICOLOR + strImage, sync);
		}

		return img;
	}

	public static void centerFrame(Window a_window)
	{
		Dimension screenSize = a_window.getToolkit().getScreenSize();
		Dimension ownSize = a_window.getSize();
		a_window.setLocation((screenSize.width - ownSize.width) / 2,
							 (screenSize.height - ownSize.height) / 2);
	}

	/**
	 * Positions a window on the screen relative to a parent window so that its position is optimised.
	 * @param a_window a Window
	 * @param a_parent the Window's parent window
	 */
	public static void positionWindow(Window a_window, Window a_parent)
	{
		Dimension parentSize = a_parent.getSize();
		Dimension ownSize = a_window.getSize();
		Point parentLocation = a_parent.getLocationOnScreen();
		a_window.setLocation(parentLocation.x + (parentSize.width / 2) - (ownSize.width / 2 ),
							 parentLocation.y + 40);
    }

}
