package me.honnold.ladderhero.service

import me.honnold.ladderhero.model.db.FileUpload
import me.honnold.ladderhero.model.db.Player
import me.honnold.ladderhero.repository.FileUploadRepository
import me.honnold.ladderhero.service.aws.S3ClientService
import me.honnold.mpq.Archive
import me.honnold.sc2protocol.Protocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.nio.file.Path

@Service
class ReplayService(
    private val s3ClientService: S3ClientService,
    private val fileUploadRepository: FileUploadRepository,
    private val playerService: PlayerService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayService::class.java)
        private val defaultProtocol = Protocol(Protocol.DEFAULT)
    }

    fun processNewReplay(fileUpload: FileUpload): Flux<Player> {
        fileUpload.status = "PROCESSING"

        return fileUploadRepository.save(fileUpload)
            .flatMap { this.s3ClientService.download(it.key) }
            .map { this.initializeReplayDecoding(it) }
            .flatMapMany { this.playerService.processPlayers(it) }
    }

    private fun initializeReplayDecoding(path: Path): Triple<Int, Archive, Protocol> {
        val archive = Archive(path)
        val header = defaultProtocol.decodeHeader(archive.userData!!.content)
        val buildNo: Long = header["m_dataBuildNum"]
        val protocol = Protocol(buildNo.toInt())

        logger.info("Identified Protocol $buildNo for $path")

        return Triple(buildNo.toInt(), archive, protocol)
    }
}