package com.newspaper.library.config;

import com.newspaper.library.config.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Duration;

/**
 * AWS S3 client configuration.
 * <p>
 * Uses DefaultCredentialsProvider which supports:
 * - Local: AWS CLI profiles, environment variables, IAM Identity Center
 * - Production: ECS task roles, EC2 instance profiles, IAM roles
 * <p>
 * Never hardcode AWS credentials.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

  private final S3Properties s3Properties;

  /**
   * Configure S3 client with:
   * - Default credentials provider (no hardcoded keys)
   * - Configured region from properties
   * - Appropriate timeouts
   * - Built-in retry strategy from SDK
   */
  @Bean
  public S3Client s3Client() {
    log.info("Initializing S3 client - Region: {}, Bucket: {}",
            s3Properties.getRegion(),
            s3Properties.getBucket());

    ClientOverrideConfiguration clientConfig = ClientOverrideConfiguration.builder()
            .apiCallTimeout(Duration.ofMillis(s3Properties.getApiCallTimeoutMs()))
            .apiCallAttemptTimeout(Duration.ofMillis(s3Properties.getApiCallTimeoutMs()))
            .build();

    ApacheHttpClient.Builder httpClientBuilder = ApacheHttpClient.builder()
            .connectionTimeout(Duration.ofMillis(s3Properties.getConnectionTimeoutMs()))
            .socketTimeout(Duration.ofMillis(s3Properties.getApiCallTimeoutMs()));

    S3Client client = S3Client.builder()
            .region(Region.of(s3Properties.getRegion()))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .overrideConfiguration(clientConfig)
            .httpClientBuilder(httpClientBuilder)
            .build();

    log.info("S3 client initialized successfully");
    return client;
  }
}
