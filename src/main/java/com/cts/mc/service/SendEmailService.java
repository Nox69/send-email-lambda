package com.cts.mc.service;

import static com.cts.mc.config.AwsSMTPConfiguration.sesClient;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private static final String PAC_NAME_ATTRIBUTE = "pac";
	private static final String ORDER_CONFIRMATION_ATTRIBUTE = "code";
	private static final String ORDER_ID_ATTRIBUTE = "orderId";

	private static final String EMPTY = "";
	private static final String PRODUCT_NAME_ATTRIBUTE = "product";

	// Type of Email Template keys
	private static final String USER_REGISTER_EMAIL_TYPE = "register-user";
	private static final String PRODUCT_REGISTER_EMAIL_TYPE = "register-product";
	private static final String PROCESS_ORDER_EMAIL_TYPE = "process-order";

	private static final Map<String, String> EMAIL_TEMPLATE = createTemplateMap();

	// Email template based on the key
	private static final String USER_REGISTER_EMAIL_TEMPLATE = "<h2>Dear %s,</h2><br><p>Thanks for your registration.<br><p>Please Login with unique permament-access-code : <b>[%s]</b>.<br><p>This is auto-generated email, kindly donot reply to this. In case of any queries, Please contact here <a href=\"http://aws.amazon.com/\">AWS</a><br><br><p>Sincerely,<br><p>The Amazon Web Services";
	private static final String PRODUCT_REGISTER_EMAIL_TEMPLATE = "<h2>Greetings from Amazon,</h2><br><p>Your Product <b>%s<b> is added Successfully.<br><p>This is auto-generated email, kindly donot reply to this. In case of any queries, Please contact here <a href=\"http://aws.amazon.com/\">AWS</a><br><br><p>Sincerely,<br><p>The Amazon Web Services";
	private static final String PROCESS_ORDER_EMAIL_TEMPLATE = "<h2>Congratulations %s,</h2><br><p>Your order <b>[%s]</b> is successfully reserved.<br><p><b>Please enter the following Order Confirmation Code [%s] to place your order successfully.</b><br><p>%s<br><p>This is auto-generated email, kindly donot reply to this. In case of any queries, Please contact here <a href=\"http://aws.amazon.com/\">AWS</a>.<br><br><p>Sincerely,<br><p>The Amazon Web Services";

	public static void sendEmail(Map<String, MessageAttribute> messageAttributes, String body) {

		try {
			String emailType = messageAttributes.get(TYPE_ATTRIBUTE).getStringValue();
			log.info("Preparing Email for [{}]", emailType);

			SendEmailRequest request = new SendEmailRequest()
					.withDestination(
							new Destination().withToAddresses(messageAttributes.get(EMAIL_ATTRIBUTE).getStringValue()))
					.withMessage(new Message()
							.withBody(new Body().withHtml(new Content().withCharset(UTF_8.toString())
									.withData(selectAndFillTemplate(emailType, messageAttributes, body))))
							.withSubject(new Content().withCharset(UTF_8.toString()).withData(SUBJECT)))
					.withSource(FROM);
			sesClient().sendEmail(request);
		} catch (Exception ex) {
			log.error("The email was not sent. Error message: {}", ex.getMessage());
		}
	}

	private static Map<String, String> createTemplateMap() {
		Object[][] resultMap = new Object[][] { { USER_REGISTER_EMAIL_TYPE, USER_REGISTER_EMAIL_TEMPLATE },
				{ PRODUCT_REGISTER_EMAIL_TYPE, PRODUCT_REGISTER_EMAIL_TEMPLATE },
				{ PROCESS_ORDER_EMAIL_TYPE, PROCESS_ORDER_EMAIL_TEMPLATE } };

		return Stream.of(resultMap).collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));
	}

	private static String selectAndFillTemplate(String emailType, Map<String, MessageAttribute> messageAttributes,
			String body) {
		switch (emailType) {
		case USER_REGISTER_EMAIL_TYPE:
			return String.format(EMAIL_TEMPLATE.get(emailType),
					messageAttributes.get(FIRST_NAME_ATTRIBUTE).getStringValue(),
					messageAttributes.get(PAC_NAME_ATTRIBUTE).getStringValue());

		case PRODUCT_REGISTER_EMAIL_TYPE:
			return String.format(EMAIL_TEMPLATE.get(emailType),
					messageAttributes.get(PRODUCT_NAME_ATTRIBUTE).getStringValue());

		case PROCESS_ORDER_EMAIL_TYPE:
			return String.format(EMAIL_TEMPLATE.get(emailType),
					messageAttributes.get(FIRST_NAME_ATTRIBUTE).getStringValue(),
					messageAttributes.get(ORDER_ID_ATTRIBUTE).getStringValue(),
					messageAttributes.get(ORDER_CONFIRMATION_ATTRIBUTE).getStringValue(), body);
		}

		return EMPTY;

	}
}