package java.net;

public class URLDecoder
	{
		public static String decode(String s)
			{
				if(s==null)
					return null;
				try
					{
						StringBuffer output=new StringBuffer();
						int i=0;
						while(i<s.length())
							{
								char c=s.charAt(i);
								if(c=='+')
									output.append(' ');
								else if(c=='%')
									{
										int k=Integer.parseInt(s.substring(i+1,i+3));
										output.append(k);
										i+=2;
									}
								else
									output.append(c);
								i++;
							}
						return output.toString();
					}
				catch(Exception e)
					{
						return null;
					}
			}
	}