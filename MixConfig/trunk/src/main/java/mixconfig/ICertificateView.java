package mixconfig;

import anon.crypto.ICertificate;

/**
 * A certificate view extracts information from a certificate that is used in a specific context.
 * @author Rolf Wendolsky
 */
public interface ICertificateView
{
	/**
	 * Update the view with a new certificate.
	 * @param a_certificate the certificate to update the view with
	 */
	public void update(ICertificate a_certificate);

}
