package me.honnold.ladderhero.service.aws

import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.*
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.*

@Service
class S3ClientService(s3Region: Region, s3CredentialsProvider: AwsCredentialsProvider, private val s3Bucket: String) {
    companion object {
        private val logger = LoggerFactory.getLogger(S3ClientService::class.java)
    }

    private val s3Client: S3AsyncClient

    init {
        val httpClient = NettyNioAsyncHttpClient.builder()
            .writeTimeout(Duration.ZERO)
            .maxConcurrency(64)
            .build()

        val serviceConfiguration = S3Configuration.builder()
            .checksumValidationEnabled(false)
            .chunkedEncodingEnabled(true)
            .build()

        this.s3Client = S3AsyncClient.builder()
            .httpClient(httpClient)
            .region(s3Region)
            .credentialsProvider(s3CredentialsProvider)
            .serviceConfiguration(serviceConfiguration)
            .build()
    }

    fun upload(filePart: FilePart): Mono<Pair<String, String>> {
        val fileKey = UUID.randomUUID().toString()

        val uploadRequest = s3Client
            .createMultipartUpload(CreateMultipartUploadRequest.builder()
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .key(fileKey)
                .bucket(this.s3Bucket)
                .build())

        val state = UploadState(this.s3Bucket, fileKey)

        logger.debug("Uploading ${filePart.filename()} to ${this.s3Bucket} with key $fileKey")

        return Mono.fromFuture(uploadRequest)
            .flatMapMany {
                if (it.sdkHttpResponse() == null || !it.sdkHttpResponse().isSuccessful)
                    throw RuntimeException("Upload failed!")

                state.uploadId = it.uploadId()
                logger.debug("Received upload id ${state.uploadId}")

                filePart.content()
            }
            .bufferUntil {
                state.buffered += it.readableByteCount()
                logger.trace("Loading data into buffers, current size ${state.buffered}")

                if (state.buffered >= 5 * 1024 * 1024) {
                    state.buffered = 0
                    true
                } else {
                    false
                }
            }
            .map { concatBuffers(it) }
            .flatMap { uploadPart(state, it) }
            .onBackpressureBuffer()
            .reduce(state, { acc, part ->
                acc.completedParts[part.partNumber()] = part

                acc
            })
            .flatMap { completeUpload(it) }
            .map {
                if (it.sdkHttpResponse() == null || !it.sdkHttpResponse().isSuccessful)
                    throw RuntimeException("Upload failed!")

                logger.info("Successful upload of ${filePart.filename()} to ${this.s3Bucket}")
                Pair(filePart.filename(), fileKey)
            }
    }

    private fun concatBuffers(buffers: List<DataBuffer>): ByteBuffer {
        logger.debug("Creating single buffer from ${buffers.size} chunks")

        var size = 0
        buffers.forEach { size += it.readableByteCount() }

        val data = ByteBuffer.allocate(size)
        buffers.forEach { data.put(it.asByteBuffer()) }

        data.rewind()
        logger.debug("Generated final buffer of size ${data.capacity()}")
        return data
    }

    private fun uploadPart(state: UploadState, buffer: ByteBuffer): Mono<CompletedPart> {
        val partNumber = ++state.partCounter
        logger.debug("Uploading part $partNumber of size ${buffer.capacity()}")

        val request = this.s3Client.uploadPart(
            UploadPartRequest.builder()
                .bucket(state.bucket)
                .key(state.fileKey)
                .partNumber(partNumber)
                .uploadId(state.uploadId)
                .contentLength(buffer.capacity().toLong())
                .build(), AsyncRequestBody.fromPublisher(Mono.just(buffer))
        )

        return Mono.fromFuture(request)
            .map {
                if (it.sdkHttpResponse() == null || !it.sdkHttpResponse().isSuccessful)
                    throw RuntimeException("Upload failed!")

                logger.debug("Completed upload of part $partNumber. Received tag ${it.eTag()}")
                CompletedPart.builder()
                    .eTag(it.eTag())
                    .partNumber(partNumber)
                    .build()
            }
    }

    private fun completeUpload(state: UploadState): Mono<CompleteMultipartUploadResponse> {
        logger.debug("Finishing upload to ${state.bucket} for ${state.fileKey}")

        val completedUpload = CompletedMultipartUpload.builder()
            .parts(state.completedParts.values)
            .build()

        return Mono.fromFuture(s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
            .bucket(state.bucket)
            .uploadId(state.uploadId)
            .multipartUpload(completedUpload)
            .key(state.fileKey)
            .build()))
    }

    class UploadState(val bucket: String, val fileKey: String) {
        var uploadId = ""
        var partCounter = 0
        var buffered = 0
        var completedParts = HashMap<Int, CompletedPart>()
    }
}