package com.sriniketh.prose.core_network.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

object WorkDescriptionSerializer : KSerializer<WorkDescription> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("WorkDescription", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): WorkDescription {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("WorkDescriptionSerializer can only be used with JSON")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> WorkDescription(element.contentOrNull)
            is JsonObject -> WorkDescription((element["value"] as? JsonPrimitive)?.contentOrNull)
            else -> WorkDescription(null)
        }
    }

    override fun serialize(encoder: Encoder, value: WorkDescription) {
        encoder.encodeString(value.value.orEmpty())
    }
}
