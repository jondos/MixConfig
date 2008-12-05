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
package mixconfig.network;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class IncomingDialog extends ConnectionDialog
{
	protected String getType()
	{
		return "ListenerInterface";
	}

	private void createDialog(final ConnectionData data, final IncomingConnectionTableModel where, boolean a_bMixOnCD)
	{
		setSize(500, 350);

		setMixOnCDEnabled(a_bMixOnCD);
		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);

		// Constraints for the labels
		GridBagConstraints lc = new GridBagConstraints();
		lc.anchor = GridBagConstraints.WEST;
		lc.insets = new Insets(5, 5, 5, 5);
		lc.gridx = 0;
		lc.gridy = 0;
		lc.weightx = 1;

		// Constraints for all the other things...
		GridBagConstraints rc = new GridBagConstraints();
		rc.anchor = GridBagConstraints.WEST;
		rc.insets = new Insets(5, 5, 5, 5);
		rc.gridx = 1;
		rc.gridy = 0;
		rc.weightx = 0;

		addTransport(data, layout, lc, rc);
		addName(data, layout, lc, rc, !a_bMixOnCD);
		addPort(data, layout, lc, rc);
		addOptions(data, layout, lc, rc, !a_bMixOnCD);
		addKeys(data, where, layout, lc, rc);

		pack();
		getFirstone().requestFocus();
	}

	public IncomingDialog(Frame parent, String title, final IncomingConnectionTableModel where, boolean a_mixOnCD)
	{
		super(parent, title);
		createDialog(null, where, a_mixOnCD);
	}
	public IncomingDialog(Frame parent, String title, final IncomingConnectionTableModel where, ConnectionData data, boolean a_mixOnCD)
	{
		super(parent, title);
		createDialog(data, where, a_mixOnCD);
	}
}
