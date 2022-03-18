package com.newrelic.instrumentation.smallrye.mutiny;

import java.util.concurrent.Callable;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

public class NRCallableWrapper<V> implements Callable<V> {
	
	private Token token = null;
	private Callable<V> delegate = null;
	private static boolean isTransformed = false;
	
	public NRCallableWrapper(Callable<V> r, Token t) {
		delegate = r;
		token = t;
		if(!isTransformed) {
			isTransformed = true;
			AgentBridge.instrumentation.retransformUninstrumentedClass(getClass());
		}
	}
	
	@Override
	@Trace(async = true)
	public V call() throws Exception {
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		if(delegate != null) {
			return delegate.call();
		}
		return null;
	}

}
