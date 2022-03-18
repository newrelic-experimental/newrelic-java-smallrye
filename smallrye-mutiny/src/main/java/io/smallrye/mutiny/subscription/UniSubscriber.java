package io.smallrye.mutiny.subscription;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class UniSubscriber<T> {

	@NewField
	public Token token = null;
	
	@Trace(async = true)
	public void onFailure(Throwable t) {
		NewRelic.noticeError(t);
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		Weaver.callOriginal();
	}
	
	@Trace(async = true)
	public void onItem(T x) {
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		Weaver.callOriginal();
	}
	
	@Trace
	public void onSubscribe(UniSubscription sub) {
		Weaver.callOriginal();
	}
}
