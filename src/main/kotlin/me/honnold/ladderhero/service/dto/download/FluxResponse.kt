package me.honnold.ladderhero.service.dto.download

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import reactor.core.publisher.Flux
import software.amazon.awssdk.services.s3.model.GetObjectResponse

class FluxResponse {
    val cf = CompletableFuture<FluxResponse>()
    var sdkResponse: GetObjectResponse? = null
    var buffer: Flux<ByteBuffer>? = null
}
