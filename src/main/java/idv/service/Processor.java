package idv.service;

import idv.aws.service.SNSClient;
import idv.aws.service.SQSClient;
import idv.init.SpringSetup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;

@Component
@Singleton
public class Processor extends TimerTask {

	private Timer timer = new Timer();
	private long interval = 1000;
	private int receiveQuantity = 5;

	@Autowired
	private Gson gson;
	@Autowired
	private SQSClient sqsClient;
	@Autowired
	private SNSClient snsClient;

	@PostConstruct
	public void init() {
		Calendar cal = Calendar.getInstance();
		Date startTime = cal.getTime();
		timer.scheduleAtFixedRate(this, startTime, interval);
	}

	@Override
	public void run() {

		String messageBody = null;
		try {
			List<Message> todoList = sqsClient.receive(receiveQuantity);
			List<Message> doneList = new ArrayList<Message>();

			// Process Message
			for(Message message : todoList) {
				boolean processResult = true;
				messageBody = message.getBody();
				// TODO: Process Message
				// TODO: Change processResult
				if(processResult) {
					doneList.add(message);
				}
			}

			// Delete Message
			if(doneList.size() > 0) {
				List<Map<String, String>> deleteResult = sqsClient.deleteBatch(doneList);
				if(deleteResult!=null) {
					snsClient.publish("Queue Message Failed to Delete!! " + gson.toJson(deleteResult));
				}
			}

		} catch (Exception e) {
			snsClient.publish("Queue Processor Exception!! MessageBody: " + messageBody + e.getMessage());
		}
	}

	@PreDestroy
	public void destroy() {
		timer.cancel();
		timer.purge();
		timer = null;
	}

	public static void main(String[] argv) throws Exception {
		ApplicationContext context = SpringSetup.getContext();
		Processor service = (Processor)context.getBean("processor");
		service.run();
	}

}