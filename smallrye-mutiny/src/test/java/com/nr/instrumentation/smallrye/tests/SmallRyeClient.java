package com.nr.instrumentation.smallrye.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.newrelic.agent.introspec.InstrumentationTestConfig;
import com.newrelic.agent.introspec.InstrumentationTestRunner;
import com.newrelic.agent.introspec.Introspector;
import com.newrelic.agent.introspec.TracedMetricData;
import com.newrelic.api.agent.Trace;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.BlockingIterable;

@RunWith(InstrumentationTestRunner.class)
@InstrumentationTestConfig(includePrefixes = {"io.smallrye.mutiny"})
public class SmallRyeClient {

	private static final String TRANSACTION1 = "OtherTransaction/Custom/com.nr.instrumentation.smallrye.tests.SmallRyeClient/uniSubOnTest";
	private static final String TRANSACTION2 = "OtherTransaction/Custom/com.nr.instrumentation.smallrye.tests.SmallRyeClient/uniSameThread";
	private static final String TRANSACTION3 = "OtherTransaction/Custom/com.nr.instrumentation.smallrye.tests.SmallRyeClient/uniEmitOnTest";
	private static final String TRANSACTION4 = "OtherTransaction/Custom/com.nr.instrumentation.smallrye.tests.SmallRyeClient/multiSameThread";
	private static final String TRANSACTION5 = "OtherTransaction/Custom/com.nr.instrumentation.smallrye.tests.SmallRyeClient/multiEmitOnTest";
	private static final String ONITEM_UNI = "Custom/UniSubscriber/onItem";
	private static final String ONITEM_MULTI_STRICT = "Custom/MultiSubscriber/StrictMultiSubscriber/onItem";
	private static final String ONITEM_MULTI_ONEMIT = "Custom/MultiSubscriber/MultiEmitOnProcessor/onItem";
	private static final String ONCOMPLETION_STRICT = "Custom/MultiSubscriber/StrictMultiSubscriber/onCompletion";
	private static final String MULT_SUB = "Java/io.smallrye.mutiny.operators.multi.MultiEmitOnOp/subscribe";
	private static final String ONCOMPLETION_ONEMIT = "Custom/MultiSubscriber/StrictMultiSubscriber/onCompletion";
	private static final String ONSUB = "Custom/UniSubscriber/onSubscribe";

	@Test
	public void testUni() {
		uniSubOnTest();
		uniSameThread();
		uniEmitOnTest();

		Introspector introspector = InstrumentationTestRunner.getIntrospector();
		int count = introspector.getFinishedTransactionCount(5000L);
		System.out.println("There are "+count+" transactions");
		assertEquals(count, 3);
		Collection<String> transactionNames = introspector.getTransactionNames();
		assertTrue("SubOn Transaction Name Not Found", transactionNames.contains(TRANSACTION1));
		assertTrue("Same Thread Transaction Name Not Found", transactionNames.contains(TRANSACTION2));
		assertTrue("EmitOn Transaction Name Not Found", transactionNames.contains(TRANSACTION3));


		Map<String, TracedMetricData> metrics = introspector.getMetricsForTransaction(TRANSACTION1);
		Set<String> keys = metrics.keySet();

		assertTrue("Main method not found",keys.contains("Java/com.nr.instrumentation.smallrye.tests.SmallRyeClient/uniSubOnTest"));
		assertTrue("Call to onItem not found",keys.contains(ONITEM_UNI));

		metrics = introspector.getMetricsForTransaction(TRANSACTION2);
		keys = metrics.keySet();

		assertTrue("Main method not found",keys.contains("Java/com.nr.instrumentation.smallrye.tests.SmallRyeClient/uniSameThread"));
		assertTrue("Call to onItem not found",keys.contains(ONITEM_UNI));
		assertTrue("Call to onItem not found",keys.contains(ONSUB));

		metrics = introspector.getMetricsForTransaction(TRANSACTION3);
		keys = metrics.keySet();

		assertTrue("Main method not found",keys.contains("Java/com.nr.instrumentation.smallrye.tests.SmallRyeClient/uniEmitOnTest"));
		assertTrue("Call to onItem not found",keys.contains(ONITEM_UNI));
		assertTrue("Call to onItem not found",keys.contains(ONSUB));

	}

	/*
	 * should see both onSubscribe and onItem
	 */
	@Trace(dispatcher = true)
	public void uniSameThread() {
		Uni<String> uni = Uni.createFrom().item(() -> "hello");
		String result = uni.await().indefinitely();
		System.out.println("Result is "+result);

	}

	/*
	 * should see onItem only
	 */
	@Trace(dispatcher = true)
	public void uniSubOnTest() {
		Uni<String> uni = Uni.createFrom().item(() -> "hello");
		Executor executor = Executors.newSingleThreadExecutor();
		String result = uni.runSubscriptionOn(executor).await().indefinitely();
		System.out.println("Result is "+result);


	}

	/*
	 * should see both onSubscribe and onItem
	 */
	@Trace(dispatcher = true)
	public void uniEmitOnTest() {
		Uni<String> uni = Uni.createFrom().item(() -> "hello");
		Executor executor = Executors.newSingleThreadExecutor();
		String result = uni.emitOn(executor).await().indefinitely();
		System.out.println("Result is "+result);
	}
	
	@Trace(dispatcher = true)
	public void multiSameThread() {
		Multi<String> multi = Multi.createFrom().items("hello","halo","bonjour","hallo");
		BlockingIterable<String> items = multi.subscribe().asIterable();
		
		Iterator<String> iterator = items.iterator();
		
		StringBuffer sb = new StringBuffer("Collected: ");
		
		while(iterator.hasNext()) {
			String item = iterator.next();
			sb.append(item);
			if(iterator.hasNext()) sb.append(", ");
		}
		System.out.println(sb.toString());
	}
	
	@Trace(dispatcher = true)
	public void multiEmitOnTest() {
		Multi<String> multi = Multi.createFrom().items("hello","halo","bonjour","hallo");
		Executor executor = Executors.newSingleThreadExecutor();
		BlockingIterable<String> items = multi.emitOn(executor).subscribe().asIterable();
		
		Iterator<String> iterator = items.iterator();
		
		StringBuffer sb = new StringBuffer("Collected: ");
		
		while(iterator.hasNext()) {
			String item = iterator.next();
			sb.append(item);
			if(iterator.hasNext()) sb.append(", ");
		}
		System.out.println(sb.toString());
	}
	
	@Test
	public void testMulti() {
		multiSameThread();
		multiEmitOnTest();
		
		Introspector introspector = InstrumentationTestRunner.getIntrospector();
		int count = introspector.getFinishedTransactionCount(5000L);
		System.out.println("There are "+count+" transactions");
		assertEquals(count, 2);
		Collection<String> transactionNames = introspector.getTransactionNames();
		
		assertTrue("Same Thread Transaction Name Not Found", transactionNames.contains(TRANSACTION4));
		
		Map<String, TracedMetricData> metrics = introspector.getMetricsForTransaction(TRANSACTION4);
		Set<String> keys = metrics.keySet();
		
		assertTrue("Failed to find traced method", keys.contains("Java/com.nr.instrumentation.smallrye.tests.SmallRyeClient/multiSameThread"));
		assertTrue("Call to onItem not found",keys.contains(ONITEM_MULTI_STRICT));
		TracedMetricData data = metrics.get(ONITEM_MULTI_STRICT);
		assertEquals(data.getCallCount(), 4);
		assertTrue("Call to onCompletion not found",keys.contains(ONCOMPLETION_STRICT));
		
		assertTrue("EmitOn Transaction Name Not Found", transactionNames.contains(TRANSACTION5));
		
		metrics = introspector.getMetricsForTransaction(TRANSACTION5);
		keys = metrics.keySet();
		
		assertTrue("", keys.contains("Java/com.nr.instrumentation.smallrye.tests.SmallRyeClient/multiEmitOnTest"));
		assertTrue("Call to onCompletion not found",keys.contains(ONCOMPLETION_STRICT));
		assertTrue("Call to onItem not found",keys.contains(ONITEM_MULTI_STRICT));
		assertTrue("Call to onCompletion not found",keys.contains(ONCOMPLETION_ONEMIT));
		assertTrue("Call to onItem not found",keys.contains(ONITEM_MULTI_ONEMIT));
		assertTrue("Call to multi sub not found",keys.contains(MULT_SUB));
		data = metrics.get(ONITEM_MULTI_STRICT);
		assertEquals(data.getCallCount(), 4);
		
		data = metrics.get(ONITEM_MULTI_ONEMIT);
		assertEquals(data.getCallCount(), 4);
		
	}
}
