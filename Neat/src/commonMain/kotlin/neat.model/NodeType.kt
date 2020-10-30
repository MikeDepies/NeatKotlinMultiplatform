package neat.model

import kotlinx.serialization.Serializable

@Serializable
enum class NodeType {
    Input, Hidden, Output
}