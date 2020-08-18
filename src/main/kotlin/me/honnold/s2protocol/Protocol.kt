package me.honnold.s2protocol

import me.honnold.s2protocol.decoder.BitDecoder
import me.honnold.s2protocol.decoder.Decoder
import me.honnold.s2protocol.decoder.VersionedBitDecoder
import me.honnold.s2protocol.model.data.Struct
import me.honnold.s2protocol.model.event.Attribute
import me.honnold.s2protocol.model.event.AttributeEvents
import me.honnold.s2protocol.model.event.Event
import me.honnold.s2protocol.model.type.TypeInfo
import me.honnold.s2protocol.util.BitBuffer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

class Protocol(build: Int) {
    companion object {
        private val EVENT_REGEX = Regex("([\\d]+):\\s+\\(([\\d]+),\\s+'([a-zA-Z.]+)'\\)")
        private val TYPE_ID_REGEX = Regex("([\\w]+)\\s+=\\s+([\\d]+)")

        const val DEFAULT = 80188
    }

    private val infos = ArrayList<TypeInfo>()

    var gameEventsTypeId = -1
    private val gameEvents = HashMap<Int, Pair<Int, String>>()

    var messageEventTypeId = -1
    private val messageEvents = HashMap<Int, Pair<Int, String>>()

    var trackerEventTypeId = -1
    private val trackerEvents = HashMap<Int, Pair<Int, String>>()

    var gameLoopDeltaTypeId = -1
    var replayUserIdTypeId = -1
    var replayHeaderTypeId = -1
    var gameDetailsTypeId = -1
    var replayInitTypeId = -1

    init {
        val resource = this::class.java.getResource("/data/$build.dat")
            ?: throw MissingResourceException("No data file for $build", null, null)

        val reader = BufferedReader(InputStreamReader(resource.openStream()))

        reader.useLines { lines ->
            val iterator = lines.iterator()

            // types
            var line: String
            while (iterator.hasNext()) {
                line = iterator.next()
                if (line.isEmpty()) break

                this.infos.add(TypeInfo.from(line))
            }

            // game events
            while (iterator.hasNext()) {
                line = iterator.next()
                if (line.isEmpty()) break

                val entry = this.readEventLine(line)
                gameEvents[entry.key] = entry.value
            }

            line = iterator.next()
            this.gameEventsTypeId = this.readTypeId(line)

            // message events
            while (iterator.hasNext()) {
                line = iterator.next()
                if (line.isEmpty()) break

                val entry = this.readEventLine(line)
                messageEvents[entry.key] = entry.value
            }

            line = iterator.next()
            this.messageEventTypeId = this.readTypeId(line)

            // tracker events
            while (iterator.hasNext()) {
                line = iterator.next()
                if (line.isEmpty()) break

                val entry = this.readEventLine(line)
                trackerEvents[entry.key] = entry.value
            }

            line = iterator.next()
            this.trackerEventTypeId = this.readTypeId(line)

            // important ids
            this.gameLoopDeltaTypeId = this.readTypeId(iterator.next())
            this.replayUserIdTypeId = this.readTypeId(iterator.next())
            this.replayHeaderTypeId = this.readTypeId(iterator.next())
            this.gameDetailsTypeId = this.readTypeId(iterator.next())
            this.replayInitTypeId = this.readTypeId(iterator.next())
        }
    }

    fun decodeHeader(contents: ByteBuffer): Struct {
        val decoder = VersionedBitDecoder(this.infos, contents)
        val header = decoder.get(this.replayHeaderTypeId)!!

        if (header !is Struct) throw DataCorruptedException("Header is incorrect class: ${header::class.simpleName}")
        return header
    }

    fun decodeDetails(contents: ByteBuffer): Struct {
        val decoder = VersionedBitDecoder(this.infos, contents)
        val details = decoder.get(this.gameDetailsTypeId)!!

        if (details !is Struct) throw DataCorruptedException("Details is incorrect class: ${details::class.simpleName}")
        return details
    }

    fun decodeInitData(contents: ByteBuffer): Struct {
        val decoder = BitDecoder(this.infos, contents)
        val initData = decoder.get(this.replayInitTypeId)!!

        if (initData !is Struct) throw DataCorruptedException("Init dat is incorrect class: ${initData::class.simpleName}")
        return initData
    }

    fun decodeAttributeEvents(contents: ByteBuffer): AttributeEvents {
        val buffer = BitBuffer(contents, ByteOrder.LITTLE_ENDIAN)
        if (!buffer.hasRemaining()) throw IllegalArgumentException("contents must have data!")

        val source = buffer.read(8).toInt()
        val mapNamespace = buffer.read(32).toInt()
        val attributeEvents =
            AttributeEvents(source, mapNamespace)

        buffer.read(32) // ignored value (length of attributes)

        while (buffer.hasRemaining()) {
            val namespace = buffer.read(32).toInt()
            val id = buffer.read(32).toInt()
            val scope = buffer.read(8).toInt()

            buffer.align()
            val valueBuffer = buffer.readBytes(4)
            val value = StandardCharsets.UTF_8.decode(valueBuffer).toString()
                .replaceFirst("\u0000", "").reversed()

            attributeEvents.addToScope(
                scope,
                Attribute(namespace, id, scope, value)
            )
        }

        return attributeEvents
    }

    fun decodeGameEvents(contents: ByteBuffer): List<Event> {
        val decoder = BitDecoder(this.infos, contents)

        return decodeEventStream(decoder, this.gameEventsTypeId, this.gameEvents)
    }

    fun decodeMessageEvents(contents: ByteBuffer): List<Event> {
        val decoder = BitDecoder(this.infos, contents)

        return decodeEventStream(decoder, this.messageEventTypeId, this.messageEvents)
    }

    fun decodeTrackerEvents(contents: ByteBuffer): List<Event> {
        val decoder = VersionedBitDecoder(this.infos, contents)

        return decodeEventStream(decoder, this.trackerEventTypeId, this.trackerEvents, false)
    }

    private fun decodeEventStream(
        decoder: Decoder,
        eventTypeId: Int,
        eventTypes: Map<Int, Pair<Int, String>>,
        decodeUserId: Boolean = true
    ): List<Event> {

        val events = ArrayList<Event>()
        var loop = 0L

        while (decoder.input.hasRemaining()) {
            val deltaTypeData = decoder.get(this.gameLoopDeltaTypeId)
            if (deltaTypeData !is Pair<*, *>) throw DataCorruptedException("Invalid game loop delta id: ${this.gameLoopDeltaTypeId} - $deltaTypeData")

            val delta = deltaTypeData.second
            if (delta !is Long) throw DataCorruptedException("Invalid game loop delta value: $delta")
            loop += delta

            val userId = if (decodeUserId) decoder.get(this.replayUserIdTypeId) as Struct else null

            val eventId = decoder.get(eventTypeId)
            if (eventId !is Long) throw DataCorruptedException("Invalid eventId value: $eventId")
            val eventType = eventTypes[eventId.toInt()]
                ?: throw Exception("Unable to parse for $eventId")

            val event = decoder.get(eventType.first)
            if (event !is Struct) throw DataCorruptedException("Invalid event value: $event")

            events.add(Event(eventId, eventType.second, loop, userId, event))
            decoder.input.align()
        }

        return events
    }

    private fun readEventLine(line: String): AbstractMap.SimpleEntry<Int, Pair<Int, String>> {
        val eventMatch = EVENT_REGEX.find(line) ?: throw IllegalArgumentException("Unable to parse event from $line")

        val key = eventMatch.groups[1]!!.value.toInt()
        val typeId = eventMatch.groups[2]!!.value.toInt()
        val name = eventMatch.groups[3]!!.value

        return AbstractMap.SimpleEntry(key, Pair(typeId, name))
    }

    private fun readTypeId(line: String): Int {
        val gameEventTypeIdMatch =
            TYPE_ID_REGEX.find(line) ?: throw IllegalArgumentException("Unable to parse type id from $line")

        return gameEventTypeIdMatch.groups[2]!!.value.toInt()
    }
}

class DataCorruptedException(s: String) : RuntimeException(s)
