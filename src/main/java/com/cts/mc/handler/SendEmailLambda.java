package com.cts.mc.handler;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import static com.cts.mc.service.SendEmailService.*;

/**
 * @author bharatkumar
 *
 */
public class SendEmailLambda implements RequestHandler<SQSEvent, String> {

	private static final String SUCCESSFUL = "Email is sent successfully";
	private static final String EMAIL_ATTRIBUTE = "email";

	private static Logger log = LoggerFactory.getLogger(SendEmailLambda.class);

	@Override
	public String handleRequest(SQSEvent request, Context context) {

		// Processing the SQS message
		SQSMessage sqsMessage = request.getRecords().get(0);

		log.info("Processing the SQS message with Id : [{}] at [{}]",
				sqsMessage.getMessageAttributes().get(EMAIL_ATTRIBUTE).getStringValue(), LocalDateTime.now());

		sendEmail(sqsMessage.getMessageAttributes(), sqsMessage.getBody());
		log.info("Email Sent Successfully");

		return SUCCESSFUL;
	}

}
