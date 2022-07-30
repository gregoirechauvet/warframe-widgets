package com.lesamy.warframewidgets

import com.lesamy.warframewidgets.common.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

import java.net.URL
import java.time.Instant

class InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

enum class State {
    DAY, NIGH;

    companion object {
        fun fromString(state: String): State {
            return when (state) {
                "day" -> DAY
                "night" -> NIGH
                else -> throw IllegalArgumentException("Unknown $state")
            }
        }
    }
}

class StateSerializer : KSerializer<State> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("State", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: State) =
        throw NotImplementedError("No serialize() method for StateSerializer")

    override fun deserialize(decoder: Decoder): State = State.fromString(decoder.decodeString())
}

@Serializable
data class CetusCycle(
    val id: String,

    @Serializable(with = StateSerializer::class)
    val state: State,

    @Serializable(with = InstantSerializer::class)
    val expiry: Instant
)

object GetUpdate {
    private val jsonDecoder = Json { ignoreUnknownKeys = true }

    suspend fun retrieveData(platform: Platform): CetusCycle {
        val param = when (platform) {
            Platform.PC -> "pc"
            Platform.PlayStation -> "ps4"
            Platform.Xbox -> "xb1"
            Platform.Switch -> "swi"
        }

        return withContext(Dispatchers.IO) {
            val content = URL("https://api.warframestat.us/$param/cetusCycle").readText()
            jsonDecoder.decodeFromString(content)
        }
    }
}