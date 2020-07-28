package me.honnold.ladderhero.service.dto.download

import org.springframework.core.io.buffer.DataBuffer
import reactor.core.publisher.Flux
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import java.util.concurrent.CompletableFuture

class FluxResponse {
    val cf = CompletableFuture<FluxResponse>()
    var sdkResponse: GetObjectResponse? = null
    var buffer: Flux<DataBuffer>? = null
}