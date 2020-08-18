package me.honnold.mpq

import me.honnold.mpq.model.BlockEntry
import me.honnold.mpq.model.HashEntry
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ArchiveTest {
    @Test(expected = InvalidArchiveException::class)
    fun invalidArchive() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/random.txt")

        Archive(resourcePath)
    }

    @Test
    fun userData() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/archive.sc2replay")

        Archive(resourcePath).use {
            val userData = it.userData
            assertNotNull(userData)
            assertEquals(
                    ByteBuffer.wrap(byteArrayOf('M'.toByte(), 'P'.toByte(), 'Q'.toByte(), 0x1B)),
                    userData.magic)
            assertEquals(512, userData.userDataSize)
            assertEquals(1024, userData.headerOffset)
            assertEquals(115, userData.userDataHeaderSize)
        }
    }

    @Test
    fun header() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/archive.sc2replay")

        Archive(resourcePath).use {
            val header = it.header

            assertEquals(
                    ByteBuffer.wrap(byteArrayOf('M'.toByte(), 'P'.toByte(), 'Q'.toByte(), 0x1A)),
                    header.magic)
            assertEquals(208, header.headerSize)
            assertEquals(68730, header.archiveSize)
            assertEquals(3, header.formatVersion)
            assertEquals(5, header.sectorSizeShift)
            assertEquals(67946, header.hashTableOffset)
            assertEquals(68458, header.blockTableOffset)
            assertEquals(32, header.hashTableEntries)
            assertEquals(17, header.blockTableEntries)
        }
    }

    @Test
    fun hashTable() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/archive.sc2replay")

        Archive(resourcePath).use {
            val table = it.hashTable

            assertEquals(32, table.size)

            val entry0: HashEntry = table[0]
            assertEquals(0xD609_C5AE.toInt(), entry0.fileHashA)
            assertEquals(0xCAA8_D159.toInt(), entry0.fileHashB)
            assertEquals(0, entry0.language)
            assertEquals(0, entry0.platform)
            assertEquals(1, entry0.fileBlock)

            val entry31: HashEntry = table[31]
            assertEquals(0x3195_2289, entry31.fileHashA)
            assertEquals(0x6A5F_FAA3, entry31.fileHashB)
            assertEquals(0, entry31.language)
            assertEquals(0, entry31.platform)
            assertEquals(7, entry31.fileBlock)
        }
    }

    @Test
    fun blockTable() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/archive.sc2replay")

        Archive(resourcePath).use {
            val table = it.blockTable

            assertEquals(17, table.size)

            val entry0: BlockEntry = table[0]
            assertEquals(0x0000_04D0, entry0.offset)
            assertEquals(578, entry0.archivedSize)
            assertEquals(578, entry0.size)
            assertEquals(0x81000200.toInt(), entry0.flags)

            val entry16: BlockEntry = table[16]
            assertEquals(0x0001_064A, entry16.offset)
            assertEquals(348, entry16.archivedSize)
            assertEquals(348, entry16.size)
            assertEquals(0x81000200.toInt(), entry16.flags)
        }
    }

    @Test
    fun files() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/archive.sc2replay")

        Archive(resourcePath).use {
            assertThat(it.files, contains(
                    "replay.attributes.events",
                    "replay.details",
                    "replay.details.backup",
                    "replay.game.events",
                    "replay.gamemetadata.json",
                    "replay.initData",
                    "replay.initData.backup",
                    "replay.load.info",
                    "replay.message.events",
                    "replay.resumable.events",
                    "replay.server.battlelobby",
                    "replay.smartcam.events",
                    "replay.sync.events",
                    "replay.sync.history",
                    "replay.tracker.events"
            ))
        }
    }

    @Test
    fun fileContents() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/archive.sc2replay")

        Archive(resourcePath).use {
            val fileContents = it.getFileContents("replay.resumable.events")

            assertEquals(12, fileContents.remaining())
            assertEquals(0x0000_282D, fileContents.int)
            assertEquals(0xFFFF_1003.toInt(), fileContents.int)
            assertEquals(0x0000_FFFF, fileContents.int)
        }
    }

    @Test
    fun fileContents_empty() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/archive.sc2replay")

        Archive(resourcePath).use {
            val fileContents = it.getFileContents("replay.sync.history")

            assertEquals(0, fileContents.remaining())
        }
    }

    @Test(expected = Exception::class)
    fun fileContents_fileDoesntExist() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/archive.sc2replay")

        Archive(resourcePath).use {
            it.getFileContents("random.txt")
        }
    }

    @Test
    fun fileContents_multi() {
        val projectDirPath = Paths.get("").toAbsolutePath()
        val resourcePath = Paths.get(projectDirPath.toString(), "/src/test/resources/last_sector_compression.s2ma")

        Archive(resourcePath).use {
            val fileContents = it.getFileContents("t3CellFlags")

            assertEquals(28000, fileContents.remaining())
            val data = (0 until 28000 / 4).map { fileContents.int }

            assertEquals(0x4C46_4354, data[0])
            assertEquals(0x0000_0000, data[data.size - 1])
        }
    }
}