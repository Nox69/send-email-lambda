package com.cts.mc.service;

import static com.cts.mc.config.AwsSMTPConfiguration.sesClient;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.events.SQSEvent.MessageAttribute;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

public class SendEmailService {

	private SendEmailService() {
		// Utility classes should not have public constructors (squid:S1118)
	}

	private static Logger log = LoggerFactory.getLogger(SendEmailService.class);

	private static final String FROM = "bharat.wolverine@gmail.com";
	private static final String SUBJECT = "Welcome to Amazon Order Processing";
	private static final String EMAIL_ATTRIBUTE = "email";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String FIRST_NAME_ATTRIBUTE = "firstName";

	private static final String REGISTER_EMAIL_TYPE = "register";
	private static final Map<String, String> EMAIL_TEMPLATE = createTemplateMap();

	private static final String REGISTER_EMAIL_TEMPLATE = "<h2>Dear %s,</h2><br><p>Thanks for your registration.<br><p>You can use your email-id and password to login to Amazon Order Processing.<br><p>This is auto-generated email, kindly donot reply to this. In case of any queries, Please contact here <a href=\"http://aws.amazon.com/\">AWS</a><br><br><p>Sincerely,<br><p>The Amazon Web Services";

	public static void sendEmail(Map<String, MessageAttribute> messageAttributes) {

		try {
			String emailType = messageAttributes.get(TYPE_ATTRIBUTE).getStringValue();
			log.info("Preparing Email for [{}]", emailType);

			SendEmailRequest request = new SendEmailRequest()
					.withDestination(new Destination()
							.withToAddresses(messageAttributes.get(EMAIL_ATTRIBUTE).getStringValue()))
					.withMessage(new Message()
							.withBody(new Body().withHtml(new Content().withCharset(UTF_8.toString())
									.withData(String.format(EMAIL_TEMPLATE.get(emailType),
											messageAttributes.get(FIRST_NAME_ATTRIBUTE).getStringValue()))))
							.withSubject(new Content().withCharset(UTF_8.toString()).withData(SUBJECT)))
					.withSource(FROM);
			sesClient().sendEmail(request);
		} catch (Exception ex) {
			log.error("The email was not sent. Error message: {}", ex.getMessage());
		}
	}

	private static Map<String, String> createTemplateMap() {
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put(REGISTER_EMAIL_TYPE, REGISTER_EMAIL_TEMPLATE);
		return Collections.unmodifiableMap(resultMap);
	}
}