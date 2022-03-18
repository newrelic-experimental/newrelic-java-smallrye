package com.newrelic.instrumentation.smallrye.mutiny;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NRRunnableScheduledFutureWrapper<V> implements RunnableScheduledFuture<V> {
	
	private Token token = null;
	private RunnableScheduledFuture<V>   delegate = null;
	private static boolean isTransformed = false;
	
	public NRRunnableScheduledFutureWrapper(RunnableScheduledFuture<V>  r, Token t) {
		delegate = r;
		token = t;
		if(!isTransformed) {
			isTransformed = true;
			AgentBridge.instrumentation.retransformUninstrumentedClass(getClass());
		}
	}
	

	@Override
	@Trace(async = true)
	public void run() {
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		if(delegate != null) {
			delegate.run();
		}
		
	}


	@Override
	@Trace(async = true)
	public boolean cancel(boolean mayInterruptIfRunning) {
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		return delegate.cancel(mayInterruptIfRunning);
	}


	@Override
	public boolean isCancelled() {
		return delegate.isCancelled();
	}


	@Override
	public boolean isDone() {
		return delegate.isDone();
	}


	@Override
	public V get() throws InterruptedException, ExecutionException {
		V v = delegate.get();
		if(token != null) {
			token.expire();
			token = null;
		}
		return v;
	}


	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		V v = delegate.get();
		if(token != null) {
			token.expire();
			token = null;
		}
		return v;
	}


	@Override
	public long getDelay(TimeUnit unit) {
		return delegate.getDelay(unit);
	}


	@Override
	public int compareTo(Delayed o) {
		return delegate.compareTo(o);
	}


	@Override
	public boolean isPeriodic() {
		return delegate.isPeriodic();
	}

}
