package ee.ttu.idu0080.raamatupood.client;

import java.math.BigDecimal;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.types.TellimuseRida;
import ee.ttu.idu0080.raamatupood.types.Tellimus;

/**
 * JMS sĆµnumite tarbija. Ć�hendub broker-i urlile
 * 
 * @author Allar Tammik
 * @date 08.03.2010
 */
public class Consumer implements MessageListener, ExceptionListener {
	protected static final Logger log = Logger.getLogger(Consumer.class);
	private static String SUBJECT = "tellimuse.edastamine";
	private static String SUBJECT_ANSWER = "tellimuse.vastus";
	private static String user = ActiveMQConnection.DEFAULT_USER;
	private static String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private static String url = EmbeddedBroker.URL;
	private static long timeToLive = 100000;
	
	private static MessageProducer msgProducer;
	private static Session session;
	//public ObjectMessage objectMessage;

	public static void main(String[] args) throws JMSException {
		Consumer consumer = new Consumer();
		
		log.info("Connecting to URL: " + url);
		log.info("Consuming queue : " + SUBJECT);
		System.out.println("Consuming queue : " + SUBJECT);
		
		// 1. Loome Ć¼henduse
		Connection connection = null;
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
		connection = connectionFactory.createConnection();
		// Kui Ć¼hendus kaob, lĆµpetatakse Consumeri tĆ¶Ć¶ veateatega.
		connection.setExceptionListener(consumer);

		// KĆ¤ivitame Ć¼henduse
		connection.start();

		// 2. Loome sessiooni
		/*
		 * createSession vĆµtab 2 argumenti: 1. kas saame kasutada
		 * transaktsioone 2. automaatne kinnitamine
		 */
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// Loome teadete sihtkoha (jĆ¤rjekorra). Parameetriks jĆ¤rjekorra nimi
		Destination destination = session.createQueue(SUBJECT);
		Destination answerDestination = session.createQueue(SUBJECT_ANSWER);

		// 3. Teadete vastuvĆµtja
		MessageConsumer msgConsumer = session.createConsumer(destination);
		msgProducer = session.createProducer(answerDestination);
		msgProducer.setTimeToLive(timeToLive);

		// Kui teade vastu vĆµetakse kĆ¤ivitatakse onMessage()
		msgConsumer.setMessageListener(consumer);
	}
	
	private void sendAnswer(String answerMsg){
		try{
			TextMessage answer = session.createTextMessage(answerMsg);
			log.debug("Sending answer: " + answer.getText());
			msgProducer.send(answer);
			
		}catch (JMSException e){
			log.warn("Caught: " + e);
			e.printStackTrace();
		}
	}
	
	

	@Override
	public void onException(JMSException ex) {
		System.out.println("JMS Exception occured. Shutting down client.");
		ex.printStackTrace();
		
	}

	@Override
	public void onMessage(Message message) {
		try{
			if (message instanceof TextMessage){
				TextMessage txtMsg = (TextMessage) message;
				String msg = txtMsg.getText();
				System.out.println("\nNew text message: " + msg);
			} else if (message instanceof ObjectMessage) {
				ObjectMessage objMsg = (ObjectMessage) message;
				
				if(objMsg.getObject() instanceof Tellimus){
					Tellimus tellimus = (Tellimus) objMsg.getObject();
					BigDecimal tellimuseSumma = BigDecimal.ZERO;
					
					List<TellimuseRida> tellimuseRead = tellimus.getTellimuseRead();
					System.out.println("Received order (" + tellimus.getTellimuseRead().size() + " products). Order: ");
					
					for (int i = 0; i < tellimuseRead.size(); i++){
						TellimuseRida rida = tellimuseRead.get(i);
						System.out.println("Product: " + rida.getToode().getNimetus());
						
						BigDecimal kogus = BigDecimal.valueOf(rida.getKogus());
						System.out.println("amount: " + kogus);
						
						BigDecimal hind = rida.getToode().getHind();
						System.out.println("price: " + hind);
						
						BigDecimal reaSumma = hind.multiply(kogus);
						System.out.println("Line total: " + reaSumma + "\n");
						
						tellimuseSumma = tellimuseSumma.add(reaSumma);
					}
					System.out.println("----------------------------");
					System.out.println("Order total: " + tellimuseSumma);
					
					String response = "Product count total: " + tellimuseRead.size() + " price total: " + tellimuseSumma + ".";
					
					sendAnswer(response);
					
				}
			}
		} catch (JMSException e){
			e.printStackTrace();
		}
	}
}