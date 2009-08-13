package mixconfig.network;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

import anon.infoservice.IMutableProxyInterface;
import anon.infoservice.IProxyInterfaceGetter;
import anon.infoservice.ImmutableProxyInterface;
import anon.infoservice.ProxyInterface;

public class ProxyAdapter implements IMutableProxyInterface
{
	
	Proxy proxy;
	
	public ProxyAdapter(Proxy proxy)
	{
		this.proxy = proxy;
	}
	
	public IProxyInterfaceGetter getProxyInterface(boolean anonInterface) 
	{
		return new IProxyInterfaceGetter()
		{
			public ImmutableProxyInterface getProxyInterface() 
			{
				SocketAddress address = proxy.address();
				if( (address != null) && 
					(address instanceof InetSocketAddress) )
				{
				 	return new ProxyInterface(((InetSocketAddress) address).getHostName(), 
				 				((InetSocketAddress) address).getPort(), null); 
				}
				return null;
			}
		};
	}
}
