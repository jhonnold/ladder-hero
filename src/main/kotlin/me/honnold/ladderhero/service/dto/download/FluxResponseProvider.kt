package me.honnold.ladderhero.service.dto.download

import org.springframework.core.io.buffer.DefaultDataBufferFactory
import reactor.core.publisher.Flux
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.core.async.SdkPublisher
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

class FluxResponseProvider : AsyncResponseTransformer<GetObjectResponse, FluxResponse> {
    private lateinit var response: FluxResponse

    override fun prepare(): CompletableFuture<FluxResponse> {
        this.response = FluxResponse()
        return this.response.cf
    }

    override fun onResponse(response: GetObjectResponse) {
        this.response.sdkResponse = response
    }

    override fun onStream(publisher: SdkPublisher<ByteBuffer>) {
        val factory = DefaultDataBufferFactory()

        this.response.buffer = Flux.from(publisher)
            .map { factory.wrap(it) }

        this.response.cf.complete(this.response)
    }

    override fun exceptionOccurred(error: Throwable?) {
        this.response.cf.completeExceptionally(error)
    }
}