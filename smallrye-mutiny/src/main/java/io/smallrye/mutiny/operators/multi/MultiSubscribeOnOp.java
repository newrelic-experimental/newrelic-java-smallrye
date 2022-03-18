package io.smallrye.mutiny.operators.multi;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

import io.smallrye.mutiny.subscription.MultiSubscriber;

@Weave
public abstract class MultiSubscribeOnOp<T> {

	public void subscribe(MultiSubscriber<? super T> downstream) {
		if(downstream.token == null) {
			Token token = NewRelic.getAgent().getTransaction().getToken();
			if(token != null && token.isActive()) {
				downstream.token = token;
			} else if(token != null) {
				token.expire();
				token = null;
			}
		}
		Weaver.callOriginal();
	}
}
