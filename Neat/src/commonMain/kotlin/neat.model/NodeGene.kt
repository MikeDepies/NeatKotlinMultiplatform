package neat.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import neat.ActivationGene


data class NodeGene(var node: Int, val nodeType: NodeType, var activationFunction: ActivationGene) {
//    var value: Float = 0f
}

@Serializable
data class SerializableNodeGene(val node: Int, val nodeType: NodeType, val activationFunction: String)


fun SerializableNodeGene.toNodeGene(activationFunctionDictionary: Map<String, ActivationGene>): NodeGene {
    return NodeGene(node, nodeType, activationFunctionDictionary.getValue(activationFunction))
}

fun NodeGene.toSerializable() = SerializableNodeGene(node, nodeType, activationFunction.name)

class NodeGeneSerializer(private val activationFunctionDictionary: Map<String, ActivationGene>) : KSerializer<NodeGene> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NodeGene", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): NodeGene {
        return decoder.decodeSerializableValue(SerializableNodeGene.serializer())
            .toNodeGene(activationFunctionDictionary)
    }

    override fun serialize(encoder: Encoder, value: NodeGene) {
        encoder.encodeSerializableValue(SerializableNodeGene.serializer(), value.toSerializable())
    }
}