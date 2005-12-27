/*
 Copyright (c) 2000, The JAP-Team
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

package mixconfig;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import gui.GUIUtils;


class BusyWindow extends JDialog implements ActionListener
{
	private SwingWorker sw;
	private JProgressBar progressBar;

	public BusyWindow(Frame parent, String reason)
	{
		super(parent,reason,true);
		setResizable(false);
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createRaisedBevelBorder());
		getContentPane().add(p);

		GridBagLayout layout = new GridBagLayout();
		p.setLayout(layout);

		// Constraints for the labels
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JComponent label = new JLabel("Please wait: " + reason);
		gbc.gridx = 0;
		gbc.weightx = 0;
		layout.setConstraints(label, gbc);
		p.add(label);
		gbc.gridy++;

		ImageIcon img = GUIUtils.loadImageIcon("busy.gif");
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(img.getImage(), 1);
		try
		{
			mt.waitForAll();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		label = new JLabel(img);
		p.add(label, gbc);
		/* Funktioniert nicht. Der Abbruch wird irgendwo abgefangen.
		 gbc.gridy++;
		 JButton button = new JButton("Cancel");
		 button.addActionListener(this);
		 layout.setConstraints(button, gbc);
		 p.add(button);
		 */

	    /* the progressbar
		progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
		//We call setStringPainted, even though we don't want the
		//string to show up until we switch to determinate mode,
		//so that the progress bar height stays the same whether
		//or not the string is shown.
		progressBar.setStringPainted(true); //get space for the string
		progressBar.setString(""); //but don't paint it
		//progressBar.setIndeterminate(true);

		p.add(progressBar); */

		this.pack();
		//Dimension d = parent.getSize();
		//Dimension d2 = this.getSize();
		//Point l = parent.getLocation();
		//this.setLocation(
		//	l.x + (d.width - d2.width) / 2,
		//	l.y + (d.height - d2.height) / 2);
		//this.setLocation(parent.getLocation());
		//this.setSize(d);
		//this.setVisible(true);

	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public void setSwingWorker(SwingWorker s)
	{
		sw = s;
	}

	public void actionPerformed(ActionEvent ev)
	{
		if (sw != null)
		{
			sw.interrupt();
		}
	}

}
