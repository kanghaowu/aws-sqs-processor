package idv.init;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringSetup {

	private static ApplicationContext context;

	public static ApplicationContext getContext() {
		if(context==null) {
			init();
		}
		return context;
	}

	private static void init() {
		context = new ClassPathXmlApplicationContext(new String[]{"applicationContext.xml"});		
	}

}