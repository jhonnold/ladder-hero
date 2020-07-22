package me.honnold.ladderhero.config.aws

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region

@Configuration
open class S3Config {
    @Value("\${aws.s3.access-key}")
    private lateinit var accessKey: String

    @Value("\${aws.s3.secret-key}")
    private lateinit var secretKey: String

    @Value("\${aws.s3.bucket}")
    private lateinit var bucket: String

    @Value("\${aws.s3.region}")
    private lateinit var region: String

    @Bean("s3CredentialsProvider")
    open fun getS3CredentialsProvider(): AwsCredentialsProvider {
        return AwsCredentialsProvider { AwsBasicCredentials.create(this.accessKey, this.secretKey) }
    }

    @Bean("s3Region")
    open fun getS3Region(): Region {
        return Region.of(region)
    }

    @Bean("s3Bucket")
    open fun getS3Bucket(): String {
        return this.bucket
    }
}