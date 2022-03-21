package io.smallrye.mutiny.subscription;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class MultiSubscriber<T> {
	
	@NewField
	public Token token = null;
	
	/*
	 * Field is needed to avoid reporting the same error twice.  This is because both onError and onFailure can both get called and it would be the same error.
	 */
	@NewField
	private boolean errorReported = false;
	

	@Trace(async = true)
	public void onItem(T item) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","MultiSubscriber",getClass().getSimpleName(),"onItem");
		if(token != null) {
			token.link();
		}
		Weaver.callOriginal();
	}
	
	@Trace(async = true)
	public  void onFailure(Throwable failure) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","MultiSubscriber",getClass().getSimpleName(),"onFailure");
		if(!errorReported) {
			NewRelic.noticeError(failure);
			errorReported = true;
		}
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		Weaver.callOriginal();
	}
	
	@Trace(async = true)
	public void onCompletion() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","MultiSubscriber",getClass().getSimpleName(),"onCompletion");
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		Weaver.callOriginal();
	}
	
	@Trace(async = true)
	public void onNext(T t) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","MultiSubscriber",getClass().getSimpleName(),"onNext");
		if(token != null) {
			token.link();
		}
		Weaver.callOriginal();
	}
	
	@Trace(async = true)
	public void onError(Throwable t) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","MultiSubscriber",getClass().getSimpleName(),"onError");
		if(!errorReported) {
			NewRelic.noticeError(t);
			errorReported = true;
		}
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		Weaver.callOriginal();
	}
	
	@Trace(async = true)
	public void onComplete() {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","MultiSubscriber",getClass().getSimpleName(),"onComplete");
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		Weaver.callOriginal();		
	}
}
