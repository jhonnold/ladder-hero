package me.honnold.ladderhero.config.balance

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.InputStreamReader

@Configuration
open class BalanceDataConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(BalanceDataConfig::class.java)
        private const val BALANCE_DATA_FILE = "/data/balance-data.bz2"
    }

    @Bean("sc2BalanceData")
    open fun sc2BalanceData(): SC2BalanceData {
        val resource = this::class.java.getResource(BALANCE_DATA_FILE)

        return BZip2CompressorInputStream(resource.openStream()).use { input ->
            val reader = InputStreamReader(input, Charsets.UTF_8)
            val parser = JSONParser()
            val json = parser.parse(reader) as JSONObject

            val data = SC2BalanceData(json)
            logger.info("Loaded Starcraft II Balance Data - ${data.locale.size} Locales and ${data.units.size} Units")

            data
        }
    }
}