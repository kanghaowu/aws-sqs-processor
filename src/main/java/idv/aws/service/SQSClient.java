package idv.aws.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

@Component
@Singleton
public class SQSClient {

	private AmazonSQS sqsClient;
	private String queueUrl;

	@Autowired
	public SQSClient(
			@Value("${AWS.Region}") String region,
			@Value("${AWS.SQS.QueueName}") String queueName,
			AWSClientConfig awsClientConfig) {

		super();
		AmazonSQSClientBuilder awsSQSClientBuilder = AmazonSQSClient.builder().withRegion(region);
		awsSQSClientBuilder.withClientConfiguration(awsClientConfig.getConfig());

		this.sqsClient = awsSQSClientBuilder.build();
		this.queueUrl = sqsClient.getQueueUrl(queueName).getQueueUrl();
	}

	/** 發送訊息 */
	public String send(String message) throws Exception {
		String messageId = null;
		SendMessageRequest request = new SendMessageRequest().withMessageBody(message).withQueueUrl(queueUrl);
		SendMessageResult result = sqsClient.sendMessage(request);
		if (result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200) {
			messageId = result.getMessageId();
		}
		return messageId;
	}

	/** 批次發送訊息 */
	public List<Map<String, String>> sendBatch(List<String> messages) throws Exception {
		List<Map<String, String>> failList = new ArrayList<Map<String, String>>();
		Collection<SendMessageBatchRequestEntry> collection = new ArrayList<SendMessageBatchRequestEntry>();
		for (int i = 0; i < messages.size(); i++) {
			SendMessageBatchRequestEntry entry = new SendMessageBatchRequestEntry();
			entry.setMessageBody(messages.get(i));
			entry.setId("index_" + i);
			collection.add(entry);
		}
		SendMessageBatchRequest request = new SendMessageBatchRequest().withEntries(collection).withQueueUrl(queueUrl);
		SendMessageBatchResult result = sqsClient.sendMessageBatch(request);
		if (result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200 && result.getFailed().size() == 0) {
			return null;
		} else {
			List<BatchResultErrorEntry> list = result.getFailed();
			for (BatchResultErrorEntry entry : list) {
				Map<String, String> failNode = new HashMap<String, String>();
				int failIndex = Integer.parseInt((entry.getId().replace("index_", "")));
				failNode.put("notification", messages.get(failIndex));
				failNode.put("message", entry.getMessage());
				failList.add(failNode);
			}
		}
		return failList;
	}

	/** 取得訊息 */
	public List<Message> receive(Integer receiveQuantity) throws Exception {
		ReceiveMessageRequest request = new ReceiveMessageRequest().withMaxNumberOfMessages(receiveQuantity).withQueueUrl(queueUrl);
		return sqsClient.receiveMessage(request).getMessages();
	}

	/** 刪除訊息 */
	public Boolean delete(Message message) throws Exception {
		Boolean event = false;
		DeleteMessageRequest request = new DeleteMessageRequest().withReceiptHandle(message.getReceiptHandle()).withQueueUrl(queueUrl);
		DeleteMessageResult result = sqsClient.deleteMessage(request);
		if (result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200) {
			event = true;
		}
		return event;
	}

	/** 批次刪除訊息 */
	public List<Map<String, String>> deleteBatch(List<Message> messages) throws Exception {
		List<Map<String, String>> failList = new ArrayList<Map<String, String>>();
		Collection<DeleteMessageBatchRequestEntry> collection = new ArrayList<DeleteMessageBatchRequestEntry>();
		for (int i = 0; i < messages.size(); i++) {
			DeleteMessageBatchRequestEntry entry = new DeleteMessageBatchRequestEntry();
			entry.setReceiptHandle(messages.get(i).getReceiptHandle());
			entry.setId("index_" + i);
			collection.add(entry);
		}
		DeleteMessageBatchRequest request = new DeleteMessageBatchRequest().withEntries(collection).withQueueUrl(queueUrl);
		DeleteMessageBatchResult result = sqsClient.deleteMessageBatch(request);
		if (result != null && result.getSdkHttpMetadata().getHttpStatusCode() == 200 && result.getFailed().size() == 0) {
			return null;
		} else {
			List<BatchResultErrorEntry> list = result.getFailed();
			for (BatchResultErrorEntry entry : list) {
				Map<String, String> failNode = new HashMap<String, String>();
				int failIndex = Integer.parseInt((entry.getId().replace("index_", "")));
				failNode.put("notification", messages.get(failIndex).getBody());
				failNode.put("message", entry.getMessage());
				failList.add(failNode);
			}
		}
		return failList;
	}

}