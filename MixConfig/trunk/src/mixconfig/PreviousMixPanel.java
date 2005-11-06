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

import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogType;

/** This panel displays information about the previous mix if the current mix is
 * a middle or last mix.
 *
 * @author Tobias Bayer
 */
public class PreviousMixPanel extends OtherMixPanel implements ChangeListener
{
	public PreviousMixPanel()
	{
		super(OtherMixPanel.MIX_TYPE_PREVIOUS);
	}

	public void setConfiguration(MixConfiguration a_conf) throws IOException
	{
		// first enable all components to make MixConfigPanel load their data
		enableComponents();

		super.setConfiguration(a_conf);

		// make sure this panel is contained only once in the config's listeners list
		a_conf.removeChangeListener(this);
		a_conf.addChangeListener(this);

		int mixType = Integer.valueOf(getConfiguration().getValue("General/MixType")).
			intValue();
		setEnabled (mixType != MixConfiguration.MIXTYPE_FIRST &&
					(!getConfiguration().isAutoConfigurationAllowed()
					 || getConfiguration().isFallbackEnabled()));

		enableComponents();
	}

	public void stateChanged(ChangeEvent e)
	{
		try
		{
			if (e instanceof ConfigurationEvent)
			{
				ConfigurationEvent c = (ConfigurationEvent) e;
				if (c.getChangedAttribute().equals("General/MixType"))
				{
					int mixType = Integer.valueOf(getConfiguration().getValue("General/MixType")).
						intValue();

					enableCert(mixType != MixConfiguration.MIXTYPE_FIRST &&
										  (!getConfiguration().isAutoConfigurationAllowed() ||
										   getConfiguration().isFallbackEnabled()));


					setEnabled (mixType != MixConfiguration.MIXTYPE_FIRST);
				}
				else if (c.getChangedAttribute().indexOf(GeneralPanel.XMLPATH_AUTOCONFIGURATION) >= 0)
				{
					enableComponents();
				}

			}

			else if (e.getSource() instanceof CertPanel)
			{
				save( (CertPanel) e.getSource());
			}
		}
		catch (Exception ex)
		{
			MixConfig.handleError(ex, null, LogType.GUI);
		}

	}
}
