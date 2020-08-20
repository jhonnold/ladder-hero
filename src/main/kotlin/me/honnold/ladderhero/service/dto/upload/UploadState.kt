package me.honnold.ladderhero.service.dto.upload

import java.util.*
import software.amazon.awssdk.services.s3.model.CompletedPart

data class UploadState(val bucket: String, val fileKey: UUID) {
    var uploadId = ""
    var partCounter = 0
    var buffered = 0
    var completedParts = HashMap<Int, CompletedPart>()
}
