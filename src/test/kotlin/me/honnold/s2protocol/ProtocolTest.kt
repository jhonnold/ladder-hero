package me.honnold.s2protocol

import me.honnold.mpq.Archive
import me.honnold.s2protocol.model.data.Blob
import me.honnold.s2protocol.model.data.Struct
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProtocolTest {
    @Test
    fun decodeHeader() {
        val resource = this::class.java.getResource("/archive.sc2replay")
        val archive = Archive(Paths.get(resource.toURI()))
        val contents = archive.userData!!.content

        val p = Protocol(Protocol.DEFAULT)
        val decodedHeader = p.decodeHeader(contents)

        assertTrue(decodedHeader["m_useScaledTime"])
        assertEquals(80188L, decodedHeader["m_dataBuildNum"])
        assertEquals(10318L, decodedHeader["m_elapsedGameLoops"])
        assertEquals(2L, decodedHeader["m_type"])

        val version: Struct = decodedHeader["m_version"]
        assertEquals(0L, version["m_revision"])
        assertEquals(80188L, version["m_build"])
        assertEquals(1L, version["m_flags"])
        assertEquals(4L, version["m_major"])
        assertEquals(12L, version["m_minor"])
        assertEquals(80188L, version["m_baseBuild"])

        val signature: Blob = decodedHeader["m_signature"]
        assertTrue(signature.value.startsWith("StarCraft II replay"))
    }

    @Test
    fun decodeDetails() {
        val resource = this::class.java.getResource("/archive.sc2replay")
        val archive = Archive(Paths.get(resource.toURI()))
        val contents = archive.getFileContents("replay.details")

        val p = Protocol(Protocol.DEFAULT)
        val decodedDetails = p.decodeDetails(contents)

        val title: Blob = decodedDetails["m_title"]
        assertEquals("Ever Dream LE", title.value)

        val players: List<Struct> = decodedDetails["m_playerList"]

        val race: Blob = players[1]["m_race"]
        assertEquals("Terran", race.value)
        val name: Blob = players[1]["m_name"]
        assertEquals("&lt;xACABx&gt;<sp/>Zomby", name.value)
    }

    @Test
    fun decodeInitData() {
        val resource = this::class.java.getResource("/archive.sc2replay")
        val archive = Archive(Paths.get(resource.toURI()))
        val contents = archive.getFileContents("replay.initData")

        val p = Protocol(Protocol.DEFAULT)
        val decodedInitData = p.decodeInitData(contents)
        val syncLobbyState: Struct = decodedInitData["m_syncLobbyState"]

        val userInitData: List<Struct> = syncLobbyState["m_userInitialData"]
        assertEquals(16, userInitData.size)
        assertEquals(5L, userInitData[1]["m_highestLeague"])

        val clanTag: Blob = userInitData[1]["m_clanTag"]
        assertEquals("xACABx", clanTag.value)
        val name: Blob = userInitData[1]["m_name"]
        assertEquals("Zomby", name.value)
        assertEquals(3908L, userInitData[1]["m_scaledRating"])

        val gameDescription: Struct = syncLobbyState["m_gameDescription"]
        assertEquals(216L, gameDescription["m_mapSizeY"])
        assertEquals(200L, gameDescription["m_mapSizeX"])
    }

    @Test
    fun decodeAttributes() {
        val resource = this::class.java.getResource("/archive.sc2replay")
        val archive = Archive(Paths.get(resource.toURI()))
        val contents = archive.getFileContents("replay.attributes.events")

        val p = Protocol(Protocol.DEFAULT)
        val events = p.decodeAttributeEvents(contents)

        assertNotNull(events)
        assertEquals(0, events.source)
        assertEquals(0, events.mapNamespace)

        assertEquals(3, events.scopes.keys.size)
        assertTrue(events.scopes.keys.contains(1))
        assertTrue(events.scopes.keys.contains(2))
        assertTrue(events.scopes.keys.contains(16))

        assertEquals(75, events.scopes[1]!!.keys.size)
        assertEquals(75, events.scopes[2]!!.keys.size)
        assertEquals(10, events.scopes[16]!!.keys.size)

        val attributeFromScope1 = events.scopes[1]!![500]
        assertNotNull(attributeFromScope1)
        assertEquals(999, attributeFromScope1.namespace)
        assertEquals(500, attributeFromScope1.id)
        assertEquals("Humn", attributeFromScope1.value)
        assertEquals(1, attributeFromScope1.scope)

        val attributeFromScope2 = events.scopes[2]!![3172]
        assertNotNull(attributeFromScope2)
        assertEquals(999, attributeFromScope2.namespace)
        assertEquals(3172, attributeFromScope2.id)
        assertEquals("AB00", attributeFromScope2.value)
        assertEquals(2, attributeFromScope2.scope)

        val attributeFromScope16 = events.scopes[16]!![1001]
        assertNotNull(attributeFromScope16)
        assertEquals(999, attributeFromScope16.namespace)
        assertEquals(1001, attributeFromScope16.id)
        assertEquals("yes", attributeFromScope16.value)
        assertEquals(16, attributeFromScope16.scope)
    }

    @Test
    fun decodeGameEvents() {
        val resource = this::class.java.getResource("/archive.sc2replay")
        val archive = Archive(Paths.get(resource.toURI()))
        val contents = archive.getFileContents("replay.game.events")

        val p = Protocol(Protocol.DEFAULT)

        val events = p.decodeGameEvents(contents)
        assertEquals(6846, events.size)

        assertEquals(2, events.count { it.name == "NNet.Game.SGameUserLeaveEvent" })
        assertEquals(540, events.count { it.name == "NNet.Game.SCmdUpdateTargetPointEvent" })
        assertEquals(2106, events.count { it.name == "NNet.Game.SControlGroupUpdateEvent" })
        assertEquals(4, events.count { it.name == "NNet.Game.STriggerSoundLengthSyncEvent" })
        assertEquals(1, events.count { it.name == "NNet.Game.SSetSyncPlayingTimeEvent" })
        assertEquals(1, events.count { it.name == "NNet.Game.SSetSyncLoadingTimeEvent" })
        assertEquals(1, events.count { it.name == "NNet.Game.SUserFinishedLoadingSyncEvent" })
        assertEquals(15, events.count { it.name == "NNet.Game.SCameraSaveEvent" })
        assertEquals(765, events.count { it.name == "NNet.Game.SSelectionDeltaEvent" })
        assertEquals(1996, events.count { it.name == "NNet.Game.SCameraUpdateEvent" })
        assertEquals(471, events.count { it.name == "NNet.Game.SCmdEvent" })
        assertEquals(2, events.count { it.name == "NNet.Game.SUserOptionsEvent" })
        assertEquals(855, events.count { it.name == "NNet.Game.SCommandManagerStateEvent" })
        assertEquals(87, events.count { it.name == "NNet.Game.SCmdUpdateTargetUnitEvent" })
    }

    @Test
    fun decodeMessageEvents() {
        val resource = this::class.java.getResource("/archive.sc2replay")
        val archive = Archive(Paths.get(resource.toURI()))
        val contents = archive.getFileContents("replay.message.events")

        val p = Protocol(Protocol.DEFAULT)

        val events = p.decodeMessageEvents(contents)

        assertEquals(2, events.count { it.name == "NNet.Game.SChatMessage" })
        assertEquals(17, events.count { it.name == "NNet.Game.SLoadingProgressMessage" })
    }

    @Test
    fun decodeTrackerEvents() {
        val resource = this::class.java.getResource("/archive.sc2replay")
        val archive = Archive(Paths.get(resource.toURI()))
        val contents = archive.getFileContents("replay.tracker.events")

        val p = Protocol(Protocol.DEFAULT)

        val events = p.decodeTrackerEvents(contents)

        assertEquals(133, events.count { it.name == "NNet.Replay.Tracker.SPlayerStatsEvent" })
        assertEquals(72, events.count { it.name == "NNet.Replay.Tracker.SUnitDoneEvent" })
        assertEquals(170, events.count { it.name == "NNet.Replay.Tracker.SUnitDiedEvent" })
        assertEquals(31, events.count { it.name == "NNet.Replay.Tracker.SUpgradeEvent" })
        assertEquals(75, events.count { it.name == "NNet.Replay.Tracker.SUnitInitEvent" })
        assertEquals(13, events.count { it.name == "NNet.Replay.Tracker.SUnitPositionsEvent" })
        assertEquals(565, events.count { it.name == "NNet.Replay.Tracker.SUnitBornEvent" })
        assertEquals(302, events.count { it.name == "NNet.Replay.Tracker.SUnitTypeChangeEvent" })
        assertEquals(2, events.count { it.name == "NNet.Replay.Tracker.SPlayerSetupEvent" })
    }
}