package dtupay.services.reporting.utilities.intramessaging.implementations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import dtupay.services.reporting.utilities.intramessaging.Message;
import dtupay.services.reporting.utilities.intramessaging.MessageQueue;


public class RabbitMqQueue implements MessageQueue {
	
	private static final String BROKER_HOST_NAME = "localhost";
	private static final String EXCHANGE_NAME = "eventsExchange";
	private static final String QUEUE_TYPE = "topic";

	private Channel channel;
	
	private String topic;

	public RabbitMqQueue(String topic) {
		this(BROKER_HOST_NAME,topic);
	}

	public RabbitMqQueue(String hostname, String topic) {
		this.topic = topic;
		channel = setUpChannel(hostname);
	}

	@Override
	public void addHandler(Class<? extends Message> event, Consumer<Message> handler) {
		try {
			String queueName = channel.queueDeclare().getQueue();
			channel.queueBind(queueName, EXCHANGE_NAME, topic);

			DeliverCallback deliverCallback = (consumerTag, delivery) -> {

				Message message = null;
				try (ByteArrayInputStream file = new ByteArrayInputStream(delivery.getBody());
						ObjectInputStream in = new ObjectInputStream(file);) {

					try {
						message = (Message) in.readObject();
					} catch (ClassNotFoundException e) {
						throw new Error(e);
					}
				}
				if (event.equals(message.getClass())) {
					handler.accept(message);
				}
			};
			channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
			});
		} catch (IOException e1) {
			throw new Error(e1);
		}

	}

	public void publish(Message message) {
		try {
			byte[] data = new byte[0];

			try (ByteArrayOutputStream file = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(file);) {

				out.writeObject(message);
				data = file.toByteArray();
			}
			channel.basicPublish(EXCHANGE_NAME, topic, null, data);
		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	private Channel setUpChannel(String hostname) {
		Channel channel;
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(hostname);
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare(EXCHANGE_NAME, QUEUE_TYPE);
		} catch (IOException | TimeoutException e) {
			throw new Error(e);
		}
		return channel;
	}
}