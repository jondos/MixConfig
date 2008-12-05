package mixconfig;

import javax.swing.event.ChangeEvent;

/** An event object representing a change in an attribute of a <CODE>MixConfiguration</CODE>
 * object
 * @author ronin &lt;ronin2@web.de&lt;
 */

public class ConfigurationEvent extends ChangeEvent
{
	/** The name of the attribute the value of which has been changed */
	private String m_modifiedPath;

	/** The new value of the changed attribute */
	private Object m_newValue;

	/** Constructs a new instance of <CODE>ConfigurationEvent</CODE>
	 * @param a_source The <CODE>MixConfiguration</CODE> object that triggered the event
	 * @param a_modifiedPath The name of the attribute that was changed
	 * @param a_newValue The new value of the changed attribute
	 */
	public ConfigurationEvent(Object a_source, String a_modifiedPath, Object a_newValue)
	{
		super(a_source);
		m_modifiedPath = a_modifiedPath;
		m_newValue = a_newValue;
	}

	/** Returns the name of the changed attribute.
	 * @return the name of the changed attribute
	 */
	public String getModifiedXMLPath()
	{
		return m_modifiedPath;
	}

	/** Returns the new value of the changed attribute.
	 * @return the new value of the changed attribute
	 */
	public Object getNewValue()
	{
		return m_newValue;
	}
}
