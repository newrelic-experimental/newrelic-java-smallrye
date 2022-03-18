package io.smallrye.mutiny.operators.uni;

import java.util.concurrent.Executor;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.UniOperator;
import io.smallrye.mutiny.subscription.UniSubscriber;

@Weave
public class UniEmitOn<I> extends UniOperator<I, I> {

	public UniEmitOn(Uni<I> upstream, Executor executor) {
		super(upstream);
	}

	@Override
	public void subscribe(UniSubscriber<? super I> subscriber) {
		processSubscriber(subscriber);
		Weaver.callOriginal();
	}

	private void processSubscriber(UniSubscriber<?> subscriber) {


		if(subscriber.token == null) {
			Token t = NewRelic.getAgent().getTransaction().getToken();
			if(t != null & t.isActive()) {
				subscriber.token = t;
			} else if(t != null) {
				t.expire();
				t = null;
			}
		}
	}



}
