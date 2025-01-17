package behaviourtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import messaging.Event;
import messaging.implementations.MessageQueueAsync;
import messaging.implementations.RabbitMqQueue;

public class TestMessageQueue {

	@Test
	public void testPublishSubscribe() {
		var q = new MessageQueueAsync();
		var done = new Object() {
			boolean value = false;
		};
		q.addHandler("event", e -> {
			done.value = true;
		});
		q.publish(new Event("event"));
		sleep(100);
		assertTrue(done.value);
	}

	@Test
	public void testHandlerExecutedTwice() {
		var q = new MessageQueueAsync();
		final var i = new Object() {
			public int value = 0;
		};
		q.addHandler("event", e -> {
			i.value++;
		});
		q.publish(new Event("event"));
		q.publish(new Event("event"));
		sleep(100);
		assertEquals(2, i.value);
	}

	@Test
	public void testPublishWithTwoHandlers() {
		var q = new MessageQueueAsync();
		var done1 = new Object() {
			boolean value = false;
		};
		var done2 = new Object() {
			boolean value = false;
		};
		q.addHandler("event", e -> {
			done1.value = true;
		});
		q.addHandler("event", e -> {
			done2.value = true;
		});
		q.publish(new Event("event"));
		sleep(100);
		assertTrue(done1.value);
		assertTrue(done2.value);
	}

	/*
	 * One handler completes a CompletableFuture waited for in another handler. That
	 * handler initiates the first handler by publishing an event.
	 */
	@Test
	public void testNoDeadlock() {
		var cf = new CompletableFuture<Boolean>();
		var done = new CompletableFuture<Boolean>();
		var q = new MessageQueueAsync();
		q.addHandler("one", e -> {
			cf.join();
			done.complete(true); // We have reached passed the blocking join.
		});
		q.addHandler("two", e -> {
			cf.complete(true);
		});
		q.publish(new Event("two"));
		q.publish(new Event("one"));
		sleep(100);
		assertTrue(done.join()); // Check that the handler for topic "one" terminated.
		assertTrue(cf.isDone()); // Check that the CompletableFuture is completed in the
									// handler for topic "two".
	}

	private void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e1) {
		}
	}

	// @Test // Only works when using RabbitMq
	public void testTopicMatching() {
		var q = new RabbitMqQueue();
		var s = new HashSet<String>();
		q.addHandler("one.*", e -> {
			s.add(e.getTopic());
		});
		q.publish(new Event("one.one"));
		q.publish(new Event("one.two"));
		sleep(100);
		var expected = new HashSet<String>();
		expected.add("one.one");
		expected.add("one.two");
		assertEquals(expected, s);
	}
	
	// @Test
	public void testDeserializationOfLists() throws InterruptedException, ExecutionException {
		var q = new RabbitMqQueue();
		CompletableFuture<List<String>> actual = new CompletableFuture<List<String>>();
		q.addHandler("list", e -> {
			actual.complete(e.getArgument(0, new TypeToken<List<String>>(){}.getType()));
		});
		List<String> expected = new ArrayList<>();
		expected.add("1");
		expected.add("2");
		q.publish(new Event("list", expected));
		actual.join();
		assertEquals(expected,actual.get());
	}
	
	@Test
	public void testDeserializationOfListsInProcessQueue() throws InterruptedException, ExecutionException {
		var q = new MessageQueueAsync();
		CompletableFuture<List<String>> actual = new CompletableFuture<List<String>>();
		q.addHandler("list", e -> {
			actual.complete(e.getArgument(0, new TypeToken<List<String>>(){}.getType()));
		});
		List<String> expected = new ArrayList<>();
		expected.add("1");
		expected.add("2");
		q.publish(new Event("list", expected));
		actual.join();
		assertEquals(expected,actual.get());
	}
}
