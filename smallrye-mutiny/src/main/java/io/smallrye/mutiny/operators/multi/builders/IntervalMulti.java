package io.smallrye.mutiny.operators.multi.builders;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

import io.smallrye.mutiny.subscription.MultiSubscriber;

@Weave
public abstract class IntervalMulti {

	@Trace
	public void subscribe(MultiSubscriber<? super Long> actual) {
		Weaver.callOriginal();
	}

	@Weave
	static final class IntervalRunnable {
		
		@NewField
		private Token token = null;

		IntervalRunnable(MultiSubscriber<? super Long> actual,
				Duration period, Duration initial, ScheduledExecutorService executor) {
			token = NewRelic.getAgent().getTransaction().getToken();
		}
		
		@Trace(async = true)
		public void run() {
			if(token != null) {
				token.linkAndExpire();
				token = null;
			}
			Weaver.callOriginal();
		}
	}
}
