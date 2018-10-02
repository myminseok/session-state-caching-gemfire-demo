package com.example.sessiondemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.serialization.pdx.provider.PdxSerializableSessionSerializer;
import org.springframework.session.data.gemfire.serialization.pdx.support.ComposablePdxSerializer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.web.bind.annotation.RequestMapping;


import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@SpringBootApplication

@EnableGemFireHttpSession(poolName = "DEFAULT",
		regionName = "test",
		maxInactiveIntervalInSeconds = 180
)
public class SessionDemoApplication {

	Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public static void main(String[] args) {
		SpringApplication.run(SessionDemoApplication.class, args);
	}

	@RequestMapping("/*")
	public String home(HttpSession session) {
		System.out.println("session = " + session);
		logger.log(Level.INFO,"session = " + session);
		return "index";
	}

	@Bean
	public HttpSessionIdResolver httpSessionStrategy() {
		return new CookieHttpSessionIdResolver();
	}

	public static final String VISIT_COUNTER = "visitCounter";
	public static final String UID = "uid";
	public static final String USER_OBJECT = "user_object";

	public static final String X_AUTH_TOKEN_KEY = "X-Auth-Token";

	@Autowired
	protected Environment env;


	@Bean
	public ClientCache gemfireCache(@Value("${app.gemfire.locators:localhost[10334]") String locators) throws IOException {

		ClientCacheFactory factory = new ClientCacheFactory();
		if (env.acceptsProfiles("cloud")) {
			Map services = new ObjectMapper().readValue(System.getenv("VCAP_SERVICES"), Map.class);
			Map credentials = (Map) ((Map) ((List) services.get("p-cloudcache")).get(0)).get("credentials");
			ClientAuthInitialize.setVCapServices(credentials);
			addLocators(factory, (List<String>) credentials.get("locators"));
			System.out.println("@@@@"+credentials.get("locators"));
		} else {
			addLocators(factory, Arrays.asList(locators.split(",")));
		}
		factory.setPoolSubscriptionEnabled(true);
		factory.setPdxSerializer(ComposablePdxSerializer.compose(
				new PdxSerializableSessionSerializer(),
				new ReflectionBasedAutoSerializer("com.example.sessiondemo.*")
		));

		return factory.create();
	}

	protected void addLocators(ClientCacheFactory clientCacheFactory, List<String> locators) {
		locators.forEach(it -> {
			ConnectionEndpoint endpoint = ConnectionEndpoint.parse(it);
			clientCacheFactory.addPoolLocator(endpoint.getHost(), endpoint.getPort());
		});
	}

}
