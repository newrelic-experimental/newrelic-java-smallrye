package io.smallrye.mutiny.operators.uni;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

import io.smallrye.mutiny.subscription.UniSubscriber;

@Weave
public abstract class UniRunSubscribeOn<I> {

	
	public void subscribe(UniSubscriber<? super I> subscriber) {
		if(subscriber.token == null) {
			Token token = NewRelic.getAgent().getTransaction().getToken();
			if(token != null && token.isActive()) {
				subscriber.token = token;
			} else if(token != null) {
				token.expire();
				token = null;
			}
		}
		
		Weaver.callOriginal();
	}
}
