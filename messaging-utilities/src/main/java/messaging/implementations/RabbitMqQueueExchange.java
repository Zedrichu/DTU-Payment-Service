package messaging.implementations;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import messaging.Event;
import messaging.MessageQueue;

public class RabbitMqQueueExchange implements MessageQueue {

	private static final String DEFAULT_HOSTNAME = "localhost";
	private static final String EXCHANGE_NAME = "eventsExchange";
	private static final String QUEUE_TYPE = "topic";

//	private Channel channel;
	private String hostname;

	public RabbitMqQueueExchange() {
		this(DEFAULT_HOSTNAME);
	}

	public RabbitMqQueueExchange(String hostname) {
		this.hostname = hostname;
//		channel = setUpChannel();
	}

	@Override
	public void publish(Event event) {
		System.out.format("[x] publish(%s)\n", event);
		String message = new Gson().toJson(event);
		var channel = setUpChannel(event.getTopic());
		try {
			channel.basicPublish(event.getTopic(), "", null, message.getBytes("UTF-8"));
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	private Channel setUpChannel(String topic) {
		Channel chan;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(hostname);
			Connection connection = factory.newConnection();
			chan = connection.createChannel();
			chan.exchangeDeclare(topic, QUEUE_TYPE,true);
		} catch (IOException | TimeoutException e) {
			throw new Error(e);
		}
		return chan;
	}

	@Override
	public void addHandler(String topic, Consumer<Event> handler) {
		System.out.format("[x] addHandler(%s)\n", topic);
		var chan = setUpChannel(topic);
		try {
			String queueName = chan.queueDeclare().getQueue();
			chan.queueBind(queueName, topic, "#");

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				String message = new String(delivery.getBody(), "UTF-8");

				Event event = new Gson().fromJson(message, Event.class);
				System.out.format("[x] executingHandler(%s)\n", event);
				handler.accept(event);
			};
			chan.basicConsume(queueName, true, deliverCallback, consumerTag -> {
			});
		} catch (IOException e1) {
			throw new Error(e1);
		}
	}

}
