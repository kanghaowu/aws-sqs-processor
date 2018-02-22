package idv.aws.service;

import idv.util.SpringPropertiesUtil;

import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.ClientConfiguration;

@Component
@Singleton
public class AWSClientConfig {

	private ClientConfiguration config;

	@Autowired
	public AWSClientConfig() {
		if (Boolean.valueOf(SpringPropertiesUtil.getProperty("ClientConfig.ProxyOn"))) {
			config = new ClientConfiguration();
			config.setProxyHost(SpringPropertiesUtil.getProperty("ClientConfig.ProxyHost"));
			config.setProxyPort(Integer.parseInt(SpringPropertiesUtil.getProperty("ClientConfig.ProxyPort")));
		}
	}

	public ClientConfiguration getConfig() {
		return this.config;
	}

}