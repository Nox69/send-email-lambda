package com.cts.mc.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

/**
 * @author bharatkumar
 *
 */
public class AwsSMTPConfiguration {

    private AwsSMTPConfiguration() {
        // Utility classes should not have public constructors (squid:S1118)
    }

    public static AWSCredentials credentials() {
        return new BasicAWSCredentials(System.getenv("AWS_SERVICE_KEY"), System.getenv("AWS_SERVICE_SECRET"));
    }

    public static AmazonSimpleEmailService sesClient() {
        return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials()))
                .withRegion(Regions.US_EAST_2).build();
    }

}
