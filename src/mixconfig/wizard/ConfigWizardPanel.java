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
package mixconfig.wizard;

import java.io.IOException;
import java.util.Vector;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logging.LogType;

import mixconfig.OwnCertificatesPanel;
import mixconfig.GeneralPanel;
import mixconfig.MixConfig;
import mixconfig.MixConfigPanel;
import mixconfig.MixConfiguration;
import mixconfig.PaymentPanel;
import mixconfig.AdvancedPanel;
import mixconfig.CascadePanel;
import mixconfig.MixOnCDPanel;
import mixconfig.PreviousMixPanel;
import mixconfig.TitledGridBagPanel;
import mixconfig.networkpanel.NextMixProxyPanel;

/**
 * A class that represents a wizard.
 * <strong>Note:</strong> This class is based on a part of ronin's wizard API.
 * @author ronin &lt;ronin2@web.de&gt;
 */
public class ConfigWizardPanel extends JPanel implements ChangeListener
{

	/** indicates a state in which the wizard may continue to the next page */
	public static final int STATE_GO = 0;

	/** indicates that the wizard is at the first page */
	public static final int STATE_BEGIN = 1;

	/** indicates that the wizard is at the last page */
	public static final int STATE_END = 2;

	/** indicates that the wizard cannot continue to the next page */
	public static final int STATE_STOP = 4;

	/** indicates that the wizard is finished */
	public static final int STATE_FINISHED = 8 | STATE_BEGIN | STATE_END;

	/** indicates that the wizard may be finished now */
	public static final int STATE_READY_TO_FINISH = STATE_END | STATE_GO;

	/** an index indicating the current wizard page */
	private int m_currentPage = 0;

	/** the layout manager for this container */
	private CardLayout m_layout = new CardLayout(4, 4);

	/** the pages of this wizard */
	private MixConfigPanel m_pages[];

	/** a <CODE>ChangeListener</CODE> listening to <CODE>ChangeEvent</CODE>s from this object */
	private Vector m_changeListener = new Vector();

	/** the current state of the wizard */
	private int m_state = STATE_BEGIN;

	/** An array of strings containing possible error messages */
	private String[] m_errors = new String[0];

	/** Constructs a new instance of <CODE>ConfigWizardPanel</CODE>
	 * @throws IOException If an I/O exception occurs while setting the configuration object
	 */
	public ConfigWizardPanel() throws IOException
	{
		setLayout(m_layout);
		setBorder(new EtchedBorder());
		m_pages = new MixConfigPanel[8];
		m_pages[0] = new MixOnCDPanel();
		m_pages[1] = new GeneralPanel();
		m_pages[2] = new OwnCertificatesPanel(false);
		m_pages[3] = new NextMixProxyPanel();
		m_pages[4] = new PreviousMixPanel();
		//m_pages[6] = new PaymentPanel(); // only for first mix
		m_pages[5] = new CascadePanel(); // only last mix and dynamic
		m_pages[6] = new AdvancedPanel();
		m_pages[7] = makeFinishPanel();

		setConfiguration(null);

		for (int i = 0; i < m_pages.length; i++)
		{
			add(m_pages[i], m_pages[i].getClass().getName());
		}

		m_layout.first(this);
		m_state = STATE_BEGIN;
	}

	/** Navigates forward one page in the wizard. */
	public void forward()
	{
		checkState();
		if ( (m_state & STATE_END) != 0 || (m_state & STATE_STOP) != 0)
		{
			throw new CannotContinueException(m_errors);
		}
		m_currentPage++;
		m_layout.next(this);

		// skip certain panels
		if(!m_pages[m_currentPage].isEnabled())
		{

			forward();
		}

		fireStateChanged();
	}

	/** Navigates backward one page in the wizard. */
	public void back()
	{
		checkState();
		if ( (m_state & STATE_BEGIN) != 0)
		{
			throw new CannotContinueException(m_errors);
		}
		m_currentPage--;
		m_layout.previous(this);

		// skip certain panels
		if( ! m_pages[m_currentPage].isEnabled())
		{
			back();
		}

		fireStateChanged();
	}

	/**
	 * Gets the current page of the wizard.
	 * @return the container currently displayed as a page in the wizard.
	 */
	public Container getCurrentPage()
	{
		return m_pages[m_currentPage];
	}

	/** Gets the current state of the wizard. This method also calls checkState().
	 * @return the current state of the wizard
	 */
	public int getState()
	{
		checkState();
		return m_state;
	}

	/**
	 * Finishes the wizard. The object that is displaying this wizard should call this method when
	 * it wants this wizard to conclude and perform the operation that it has prepared.<br>
	 * This implementation sets the state to STATE_FINISHED and calls fireStateChanged().<br>
	 * Subclasses should override this method to implement their own way of finishing the wizard. The overridden
	 * method should call this method, or set the wizard's state to STATE_FINISHED and call fireStateChanged().
	 */
	public void finish()
	{
		m_state = STATE_BEGIN;
		fireStateChanged();
	}

	/** Adds a ChageListener to receive ChangeEvents.
	 * @param cl the <CODE>ChangeListener</CODE> to be added
	 */
	public void addChangeListener(ChangeListener cl)
	{
		m_changeListener.addElement(cl);
	}

	/** Removes the specified ChageListener from the list of ChangeListeners registered with this component.
	 * @param cl the <CODE>ChangeListener</CODE> to be removed
	 */
	public void removeChangeListener(ChangeListener cl)
	{
		m_changeListener.removeElement(cl);
	}

	/** Called when the state of this wizard changes. This implementation calls fireStateChanged() to re-cast
	 * the event.
	 * @param e a <CODE>ChangeEvent</CODE>
	 */
	public void stateChanged(ChangeEvent e)
	{
		fireStateChanged();
	}

	public Vector check()
	{
		Vector errors = new Vector();
		Vector temp;

		for (int i = 0; i < m_pages.length - 1; i++)
		{
			if (m_pages[i].isEnabled())
			{
				temp = m_pages[i].check();
				for (int j = 0; j < temp.size(); j++)
				{
					errors.addElement(temp.elementAt(j));
				}
			}
		}

		return errors;
	}

	/**
	 * Notifies all ChangeListeners registered with this object that this object's state has changed.
	 */
	protected void fireStateChanged()
	{
		//Iterator i = m_changeListener.iterator();
		// Iterator may not be used; compatibility with JRE 1.1.8 must be provided
		for (int i = 0; i < m_changeListener.size(); i++)
		{
			( (ChangeListener) m_changeListener.elementAt(i)).stateChanged(new ChangeEvent(this));
		}
	}

	/** Gets the altered state of the wizard; this method is needed by checkState() and should be overridden by subclasses of this class.
	 * @return the new state of the wizard
	 */
	protected int getNewState()
	{

		int newstate = STATE_GO;

		m_errors = new String[0];

		if (m_currentPage == 0)
		{
			newstate = newstate | STATE_BEGIN;

			// check validity of data
		}
		Vector errors = null;

		if (m_pages[m_currentPage].isEnabled() &&
			m_pages[m_currentPage] instanceof MixConfigPanel)
		{
			errors = ( (MixConfigPanel) m_pages[m_currentPage]).check();
		}
		if (errors != null && errors.size() > 0)
		{
			newstate = newstate | STATE_STOP;

			// MixConfig is meant to be compatible with JRE 1.1.8, so
			// we can't use Vector.toArray()
			m_errors = new String[errors.size()];
			for (int i = 0; i < m_errors.length; i++)
			{
				m_errors[i] = (String) errors.elementAt(i);
			}
		}

		if (m_currentPage == m_pages.length - 1)
		{
			newstate = newstate | STATE_READY_TO_FINISH;
		}

		if (m_currentPage >= m_pages.length)
		{
			newstate = newstate | STATE_END;
		}

		return newstate;
	}

	/**
	 * Calls the load-method of each Panel
	 * This is necessary if you change the view (expert|wizard)
	 */
	protected void load()
	{
		try
		{
			for (int i = 0; i < m_pages.length; i++)
			{
				m_pages[i].load();
			}
		}
		catch (Exception io)
		{
			MixConfig.handleError(io, "Error on loading the MixConfiguration", LogType.MISC);
		}
		while (!m_pages[getCurrentPageNr()].isEnabled())
		{
			back();
		}
	}


	/** Set the panel to first leaf.
	 */
	protected void reset()
	{
		m_layout.first(this);
		m_state = STATE_BEGIN;
		m_currentPage = 0;
	}



	/** Checks whether the state of the wizard has changed, e.g. when the user has entered data on a page. */
	private void checkState()
	{
		int oldstate = m_state;
		m_state = getNewState();
		if (oldstate != m_state)
		{
			fireStateChanged();
		}
	}

		/** Sets the specified configuration object. If the object is <CODE>null</CODE>, the
		 * object returned by <CODE>mixconfig.MixConfig.getMixConfiguration()</CODE> is
		 * set instead.<br>
		 * This method will also call the <CODE>setConfiguration(MixConfiguration)</CODE>
		 * method of all contained <CODE>mixconfig.MixConfigPanel</CODE> instances.
		 * @param m A <CODE>MixConfiguration</CODE> object to be edited in the wizard
		 * @throws IOException If an I/O error occurs while setting the configuration
		 */
	protected void setConfiguration(MixConfiguration m) throws IOException
	{
		if (m == null)
		{
			m = MixConfig.getMixConfiguration();
		}

		for (int i = 0; i < m_pages.length - 1; i++)
		{
			( (MixConfigPanel) m_pages[i]).setConfiguration(m);
		}
	}

	/**
	 * Constructs a panel that shows some text to inform the user that the wizard
	 * is at its end
	 * @return a <code>JPanel</code> showing a message
	 */
	private MixConfigPanel makeFinishPanel()
	{
		final ConfigWizardPanel panelWizard = this;
		MixConfigPanel finish = new MixConfigPanel("Finish") {
			// this is an anonymous class to insert it into the common MixConfigPanel array
			public Vector check() {return panelWizard.check();}
			public void enableComponents() {}};
		String text[] =
			{
			"Mix configuration finished. Click the 'Finish' button to",
			"save the configuration and quit this wizard.",
			"Click the 'Back' button if you want to make changes before",
			"saving.",
			"Click the 'Cancel' button to quit without saving."
		};

		TitledGridBagPanel panelMessage = new TitledGridBagPanel();
		panelMessage.removeInsets();

		for (int i = 0; i < text.length; i++)
		{
			panelMessage.addRow(new JLabel(text[i]), null);
		}

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.CENTER;
		finish.add(panelMessage, gbc);

		return finish;
	}

	/** Gets the current page number of the wizard.
	 * @return the current page number of the wizard
	 */
	public int getCurrentPageNr()
	{
		return m_currentPage;
	}

	/**
	 * Gets the number of pages in this wizard
	 * @return int
	 */
	public int getPageCount()
	{
		return m_pages.length;
	}

	public MixConfigPanel getPage(int a_number)
	{
		return m_pages[a_number];
	}

}
