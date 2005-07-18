/*
 Copyright (c) 2000 - 2005, The JAP-Team
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

import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import logging.*;

/**
 * This class implements an image that automatically scales its content
 * when the component is resized.
 *
 * @author Tobias Bayer
 */
public class AutoScaleImage extends JLabel
{
	private Image m_image;
	private boolean mb_smooth;

	/**
	 * Constructs a new instance of AutoScaleImage. The second argument tells the
	 * instance whether to scale smooth but slow (true) or fast but dirty (false).
	 *
	 * @param a_image ImageIcon
	 * @param a_smooth boolean
	 */
	public AutoScaleImage(ImageIcon a_image, boolean a_smooth)
	{
		m_image = a_image.getImage();
		mb_smooth = a_smooth;
		this.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				Image scaledImage;
				try
				{
					if (mb_smooth)
					{
						scaledImage = m_image.getScaledInstance(getWidth(), getHeight(),
							Image.SCALE_SMOOTH);
					}
					else
					{
						scaledImage = m_image.getScaledInstance(getWidth(), getHeight(),
							Image.SCALE_FAST);
					}
					ImageIcon imageIcon = new ImageIcon(scaledImage);
					setIcon(imageIcon);
				}
				catch (Exception a_e)
				{
					LogHolder.log(LogLevel.DEBUG, LogType.GUI, "Cannot resize image to size below 0");
				}

			}
		}
		);
	}
}
