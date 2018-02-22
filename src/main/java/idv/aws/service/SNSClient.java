package idv.aws.service;

import idv.util.SpringPropertiesUtil;

import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.util.StringUtils;

@Component
@Singleton
public class SNSClient {

	private AmazonSNS snsClient;
	private String topicArn;

	@Autowired
	public SNSClient(
			@Value("${AWS.Region}") String region,
			@Value("${AWS.SNS.TopicName}") String topicName,
			AWSClientConfig awsClientConfig) {

		super();
		AmazonSNSClientBuilder awsSNSClientBuilder = AmazonSNSClient.builder().withRegion(region);
		awsSNSClientBuilder.withClientConfiguration(awsClientConfig.getConfig());

		this.snsClient = awsSNSClientBuilder.build();
		if (!StringUtils.isNullOrEmpty(topicName)) {
			this.topicArn = snsClient.createTopic(topicName).getTopicArn();
		}
	}

	public void publish(String message) {
		PublishRequest publishRequest = new PublishRequest(topicArn, message);
		publishRequest.setSubject(SpringPropertiesUtil.getProperty("ProjectName").toUpperCase() + " Notification!!");
		try {
			snsClient.publish(publishRequest);
		} catch (AmazonClientException e) {
			System.out.println(e);
		}
	}

}