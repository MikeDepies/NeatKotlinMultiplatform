import kotlin.random.*

fun initializeNeatModel(random: Random): NeatMutator {
    return neatMutator(1, 1, random).apply {
        val node = newNode()
        val node2 = newNode()
        addNode(node)
        addNode(node2)
        addConnection(
            connectNodes(
                inputNodes.first(),
                node,
                randomWeight(random),
                connections.last().innovation + 1
            )
        )
        addConnection(
            connectNodes(
                inputNodes.first(),
                node2,
                randomWeight(random),
                connections.last().innovation + 1
            )
        )
        addConnection(
            connectNodes(
                node,
                outputNodes.last(),
                randomWeight(random),
                connections.last().innovation + 1
            )
        )
        addConnection(
            connectNodes(
                node2,
                outputNodes.last(),
                randomWeight(random),
                connections.last().innovation + 1
            )
        )

    }
}