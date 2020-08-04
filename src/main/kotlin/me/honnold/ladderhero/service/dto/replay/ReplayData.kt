package me.honnold.ladderhero.service.dto.replay

import me.honnold.mpq.Archive
import me.honnold.s2protocol.Protocol
import me.honnold.s2protocol.model.data.Struct
import me.honnold.s2protocol.model.event.Event
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class ReplayData(path: Path) {
    companion object {
        private const val METADATA_FILE_NAME = "replay.gamemetadata.json"
        private const val INIT_DATA_FILE_NAME = "replay.initData"
        private const val DETAILS_FILE_NAME = "replay.details"
        private const val TRACKER_EVENTS_FILE_NAME = "replay.tracker.events"
        private const val GAME_EVENTS_FILE_NAME = "replay.game.events"
    }

    private val protocol: Protocol

    val metadata: JSONObject
    val initData: Struct
    val details: Struct
    val trackerEvents: List<Event>
    val gameEvents: List<Event>

    init {
        val archive = Archive(path)
        val userDataContents =
            archive.userData?.content ?: throw IllegalArgumentException("Path $path is not a Starcraft II Replay!")

        val defaultProtocol = Protocol(Protocol.DEFAULT)
        val header = defaultProtocol.decodeHeader(userDataContents)
        val replayBuildNumber: Long = header["m_dataBuildNum"]

        this.protocol = Protocol(replayBuildNumber.toInt())

        val metadataBuffer = archive.getFileContents(METADATA_FILE_NAME)
        val metadataString = StandardCharsets.UTF_8.decode(metadataBuffer).toString()
        this.metadata = JSONParser().parse(metadataString) as JSONObject

        this.initData = this.protocol.decodeInitData(archive.getFileContents(INIT_DATA_FILE_NAME))
        this.details = this.protocol.decodeDetails(archive.getFileContents(DETAILS_FILE_NAME))
        this.trackerEvents = this.protocol.decodeTrackerEvents(archive.getFileContents(TRACKER_EVENTS_FILE_NAME))
        this.gameEvents = this.protocol.decodeGameEvents(archive.getFileContents(GAME_EVENTS_FILE_NAME))

        archive.close()
    }
}