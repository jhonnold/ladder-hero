package me.honnold.ladderhero.service

import me.honnold.ladderhero.service.dto.download.FluxResponseProvider
import me.honnold.ladderhero.service.dto.upload.UploadResult
import me.honnold.ladderhero.service.dto.upload.UploadState
import me.honnold.ladderhero.util.BuffersUtil
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.*
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.*

@Service
class S3ClientService(s3Region: Region, s3CredentialsProvider: AwsCredentialsProvider, private val s3Bucket: String) {
    companion object {
        private val logger = LoggerFactory.getLogger(S3ClientService::class.java)
        private val TEMP_DIR = System.getProperty("java.io.tmpdir")
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

    fun upload(filePart: FilePart): Mono<UploadResult> {
        val fileKey = UUID.randomUUID()
        val state = UploadState(this.s3Bucket, fileKey)
        val uploadRequest = s3Client.createMultipartUpload(
            CreateMultipartUploadRequest.builder()
                .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .key(fileKey.toString())
                .bucket(this.s3Bucket)
                .build()
        )

        return Mono.fromFuture(uploadRequest)
            .doOnSubscribe { logger.debug("Uploading ${filePart.filename()} to ${this.s3Bucket} with key $fileKey") }
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
            .map { BuffersUtil.concatBuffers(it) }
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

                UploadResult(fileKey, filePart.filename())
            }
            .doOnSuccess { logger.info("Successfully uploaded $it to ${this.s3Bucket}") }
            .doOnError { t -> logger.error("There was an issue uploading ${filePart.filename()} -- ${t.message}") }
            .onErrorResume { Mono.empty() }
    }

    fun download(uuid: UUID): Mono<Path> {
        val path = Paths.get(TEMP_DIR, "$uuid.SC2Replay")
        logger.debug("Requesting file key $uuid")

        val request = GetObjectRequest.builder()
            .bucket(this.s3Bucket)
            .key(uuid.toString())
            .build()

        val requestMono = this.s3Client.getObject(request, FluxResponseProvider()).toMono()

        val data = Flux.from(requestMono)
            .flatMap {
                val sdkHttpResponse = it.sdkResponse?.sdkHttpResponse()
                if (sdkHttpResponse == null || !sdkHttpResponse.isSuccessful)
                    throw RuntimeException("Failed to download file $uuid")

                it.buffer
            }

        return DataBufferUtils.write(data, path, StandardOpenOption.CREATE_NEW)
            .doOnSuccess { logger.info("Saved file $uuid to $path") }
            .doOnError { logger.error(it.message) }
            .then(Mono.just(path))
    }

    private fun uploadPart(state: UploadState, buffer: ByteBuffer): Mono<CompletedPart> {
        val partNumber = ++state.partCounter
        val request = this.s3Client.uploadPart(
            UploadPartRequest.builder()
                .bucket(state.bucket)
                .key(state.fileKey.toString())
                .partNumber(partNumber)
                .uploadId(state.uploadId)
                .contentLength(buffer.capacity().toLong())
                .build(), AsyncRequestBody.fromPublisher(Mono.just(buffer))
        )

        return Mono.fromFuture(request)
            .doOnSubscribe { logger.debug("Uploading part $partNumber of size ${buffer.capacity()}") }
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

        return Mono.fromFuture(
            this.s3Client.completeMultipartUpload(
                CompleteMultipartUploadRequest.builder()
                    .bucket(state.bucket)
                    .uploadId(state.uploadId)
                    .multipartUpload(completedUpload)
                    .key(state.fileKey.toString())
                    .build()
            )
        )
    }
}