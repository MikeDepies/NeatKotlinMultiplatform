typealias ComputationStrategy = () -> Unit
fun Set<NodeGene>.activate(map: Map<NodeGene, NetworkNode>) = forEach { map.getValue(it).activate() }
fun NeatMutator.getComputationStrategy(
    networkNodeMap: Map<NodeGene, NetworkNode>,
    idNodeMap: Map<Int, NodeGene>
): ComputationStrategy {
    val computationSequence = computationSequence(networkNodeMap, idNodeMap)
    val outputNodeSet = outputNodes.map { networkNodeMap.getValue(it) }
    return {
        computationSequence.forEach { it() }
        println()
        println("output")
        outputNodeSet.forEach { it.activate() }
        println()
    }

}

fun NeatMutator.computationSequence(
    networkNodeMap: Map<NodeGene, NetworkNode>,
    idNodeMap: Map<Int, NodeGene>
): Sequence<() -> Unit> {
    return sequence {
        val activationSet = mutableSetOf<NodeGene>()
        var activeSet = inputNodes.toSet()
        fun networkNotFullyActivated() = (activationSet.size + outputNodes.size) < nodes.size
        while (networkNotFullyActivated()) {
            val capturedSet = activeSet
            val connections = capturedSet.flatMap { node ->
                connectionsFrom(node)
            }

            val nextNodeMap = connections.groupBy { idNodeMap.getValue(it.outNode) }
            val fn = {
                capturedSet.activate(networkNodeMap)
                connections.forEach { connection ->
                    val inputValue = idNodeMap.getValue(connection.inNode)
                    val outValue = idNodeMap.getValue(connection.outNode)
                    val inputNode = networkNodeMap.getValue(inputValue)
                    val outputNode = networkNodeMap.getValue(outValue)
                    outputNode.value += inputNode.activatedValue * connection.weight
                }

            }
            activeSet.forEach { activationSet += it }
            activeSet = nextNodeMap.keys.filter { it !in activationSet }.toSet()
            yield(fn)
        }
    }
}