package mixconfig.wizard;

import java.io.IOException;
import java.util.Vector;

import java.awt.CardLayout;
import java.awt.Container;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mixconfig.CertificatesPanel;
import mixconfig.DescriptionPanel;
import mixconfig.GeneralPanel;
import mixconfig.MixConfig;
import mixconfig.MixConfigPanel;
import mixconfig.MixConfiguration;
import mixconfig.PaymentPanel;
import mixconfig.networkpanel.NetworkPanel;

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
	private Container m_pages[];

	/** a <CODE>ChangeListener</CODE> listening to <CODE>ChangeEvent</CODE>s from this object */
	private Vector m_changeListener = new Vector();

	/** the current state of the wizard */
	private int m_state = STATE_BEGIN;

	/** An array of strings containing possible error messages */
	private String[] m_errors = null;

	/** Constructs a new instance of <CODE>ConfigWizardPanel</CODE>
	 * @throws IOException If an I/O exception occurs while setting the configuration object
	 */
	public ConfigWizardPanel() throws IOException
	{
		setLayout(m_layout);
		setBorder(new EtchedBorder());
		m_pages = new Container[6];
		m_pages[0] = new GeneralPanel();
		m_pages[1] = new NetworkPanel();
		m_pages[2] = new CertificatesPanel();
		m_pages[3] = new DescriptionPanel(false);
		m_pages[4] = new PaymentPanel();
		m_pages[5] = makeFinishPanel();

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
		m_state = STATE_FINISHED;
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

		m_errors = null;

		if (m_currentPage == 0)
		{
			newstate = newstate | STATE_BEGIN;

			// check validity of data
		}
		Vector errors = null;

		if (m_pages[m_currentPage] instanceof MixConfigPanel)
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

	private void setConfiguration(MixConfiguration m) throws IOException
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
	private JPanel makeFinishPanel()
	{
		JPanel finish = new JPanel();
		finish.setLayout(new BoxLayout(finish, BoxLayout.Y_AXIS));
		String text[] =
			{
			"Mix configuration finished. Click the 'Finish' button to",
			"save the configuration and quit this wizard.",
			"Click the 'Back' button if you want to make changes before",
			"saving.",
			"Click the 'Close' button to quit without saving."
		};

		finish.setBorder(new EmptyBorder(10, 10, 10, 10));

		for (int i = 0; i < text.length; i++)
		{
			finish.add(new JLabel(text[i]));
		}

		finish.add(Box.createVerticalGlue());

		return finish;
	}
}
