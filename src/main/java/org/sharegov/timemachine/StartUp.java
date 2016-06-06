package org.sharegov.timemachine;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StartUp
{
	public static void main(String []argv)
	{
		new ClassPathXmlApplicationContext("config.xml");
		Component server = new Component();
	    server.getServers().add(Protocol.HTTP, 9192);
	    //server.getClients().add(Protocol.HTTP);
	    //server.getClients().add(Protocol.FILE);

	    final JaxRsApplication app = new JaxRsApplication(server.getContext().createChildContext());
	    app.add(new TimeMachineRestApplication());
	    server.getDefaultHost().attach(app);
	    try
		{
			server.start();
			//new ClassPathXmlApplicationContext("config.xml");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
