package mixconfig;

import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.asn1.x509.DSAParameter;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;

public class MyDSAParams extends DSAParameters implements DSAParams
	{
		public MyDSAParams()
			{
				super(null,null,null);
			}

		public MyDSAParams(DSAParams params)
			{
				super(params.getP(),params.getQ(),params.getG());
			}

		public MyDSAParams(DSAParameter params)
			{
				super(params.getP(),params.getQ(),params.getG());
			}

		public MyDSAParams(DSAParameters params)
			{
				super(params.getP(),params.getQ(),params.getG(),params.getValidationParameters());
			}

		public MyDSAParams(DSAPrivateKey key)
			{
				this(key.getParams());
			}


	}