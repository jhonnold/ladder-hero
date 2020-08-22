package me.honnold.ladderhero.service.dto.download

import reactor.core.publisher.Flux
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

class FluxResponse {
    val cf = CompletableFuture<FluxResponse>()
    var sdkResponse: GetObjectResponse? = null
    var buffer: Flux<ByteBuffer>? = null
}
