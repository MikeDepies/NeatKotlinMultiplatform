package neat.model

import neat.ActivationGene

data class NodeGene(var node: Int, val nodeType: NodeType, var activationFunction: ActivationGene) {
//    var value: Float = 0f
}