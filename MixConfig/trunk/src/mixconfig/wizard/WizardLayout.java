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


package mixconfig.wizard;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

import gui.AutoScaleImage;
import gui.JAPHelp;
import gui.JAPHelpContext;
import mixconfig.MixConfig;
import gui.GUIUtils;

public abstract class WizardLayout extends JPanel implements JAPHelpContext.IHelpContext
{

	/** The paths to the logo parts to display at the top  of the wizard */
	private static final String IMG_LEFT =	WizardLayout.class.getName() + "_left.gif";
	private static final String IMG_CENTER = WizardLayout.class.getName() + "_center.gif";
	private static final String IMG_RIGHT = WizardLayout.class.getName() + "_right.gif";

	/** The forward navigation button */
		private JButton m_bttnForward;

		/** The back navigation button */
		private JButton m_bttnBack;

		/** The cancel navigation button */
		private JButton m_bttnCancel;

	public WizardLayout()
	{
		setLayout(new BorderLayout());

	   //Create the image at the top
		JLabel leftImage = new JLabel(GUIUtils.loadImageIcon(IMG_LEFT));
		AutoScaleImage centerImage = new AutoScaleImage(GUIUtils.loadImageIcon(IMG_CENTER), false);
		JLabel rightImage = new JLabel(GUIUtils.loadImageIcon(IMG_RIGHT));
		GridBagConstraints topImageConstraints = new GridBagConstraints();
		JPanel topImage = new JPanel(new GridBagLayout());

		topImageConstraints.gridx = 0;
		topImageConstraints.gridy = 0;
		topImageConstraints.weightx = 0.0;
		topImageConstraints.weighty = 0.0;
		topImageConstraints.fill = GridBagConstraints.BOTH;
		topImage.add(leftImage, topImageConstraints);

		topImageConstraints.gridx = 1;
		topImageConstraints.weightx = 1.0;
		topImage.add(centerImage, topImageConstraints);

		topImageConstraints.gridx = 2;
		topImageConstraints.weightx = 0.0;
		topImage.add(rightImage, topImageConstraints);


		m_bttnBack = new JButton("<- Back");
		m_bttnCancel = new JButton("Cancel");
		m_bttnForward = new JButton("Next ->");

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(JAPHelp.createHelpButton(this));
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(m_bttnBack);
		buttonBox.add(m_bttnForward);
		buttonBox.add(m_bttnCancel);

		add(topImage, BorderLayout.NORTH);
		add(buttonBox, BorderLayout.SOUTH);
	}

	protected JButton getButtonBack()
	{
		return this.m_bttnBack;
	}

	protected JButton getButtonForward()
	{
		return this.m_bttnForward;
	}

	protected JButton getButtonCancel()
	{
		return this.m_bttnCancel;
	}

}
