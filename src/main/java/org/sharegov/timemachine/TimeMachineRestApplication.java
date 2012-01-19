package org.sharegov.timemachine;

import java.util.HashSet;

import java.util.Set;

import javax.ws.rs.core.Application;
import org.sharegov.timemachine.JsonEntityProvider;

public class TimeMachineRestApplication extends Application
{
	@Override
	public Set<Class<?>> getClasses()
	{
		HashSet<Class<?>> S = new HashSet<Class<?>>();
		S.add(JsonEntityProvider.class);		
		S.add(TimeMachineService.class);		
		return S;
	}
}