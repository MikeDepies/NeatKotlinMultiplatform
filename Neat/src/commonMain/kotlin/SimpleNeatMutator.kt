class SimpleNeatMutator(_nodes: List<NodeGene>, _connections: List<ConnectionGene>) : NeatMutator {
    private val _connectableNodes = resolvePotentialConnections(_nodes, _connections).toMutableList()



    private fun resolvePotentialConnections(
        _nodes: List<NodeGene>,
        _connections: List<ConnectionGene>
    ): List<PotentialConnection> {
        return _nodes.flatMap { sourceNode ->
            eligibleNodes(_nodes, sourceNode).map { targetNode ->
                PotentialConnection(sourceNode.node, targetNode.node)
            }
        }.filter { it.alreadyConnected(_connections) }
    }

    private fun eligibleNodes(
        _nodes: List<NodeGene>,
        sourceNode: NodeGene
    ) = _nodes.filterNot {
        val bothAreOutputs = bothAreOutputNodes(sourceNode, it)
        val bothAreInputs = bothAreInputs(sourceNode, it)
        bothAreInputs || bothAreOutputs
    }

    private fun bothAreInputs(sourceNode: NodeGene, it: NodeGene) =
        sourceNode.nodeType == NodeType.Input && it.nodeType == NodeType.Input

    private fun bothAreOutputNodes(sourceNode: NodeGene, it: NodeGene) =
        sourceNode.nodeType == NodeType.Output && it.nodeType == NodeType.Output

    override val nodes = _nodes.toMutableList()
    override val connections = _connections.toMutableList()
    override val hiddenNodes: List<NodeGene>
        get() = nodes.filter { it.nodeType == NodeType.Hidden }
    override val outputNodes: List<NodeGene>
        get() = nodes.filter { it.nodeType == NodeType.Output }
    override val inputNodes: List<NodeGene>
        get() = nodes.filter { it.nodeType == NodeType.Input }
    override val lastNode: NodeGene
        get() = nodes.last()
    override val connectableNodes: List<PotentialConnection>
        get() = _connectableNodes.toList()

    override fun addConnection(connectionGene: ConnectionGene) {
        if (nodes.none { it.node == connectionGene.inNode })
            error("No matching node to connect to ${connectionGene.inNode}")
        if (nodes.none { it.node == connectionGene.outNode })
            error("No matching node to connect to ${connectionGene.outNode}")
        if (connections.any { it.inNode == connectionGene.inNode && it.outNode == connectionGene.outNode })
            error("Can not add a connection gene between already connected nodes")
        connections += connectionGene
        _connectableNodes -= PotentialConnection(connectionGene.inNode, connectionGene.outNode)
    }

    override fun addNode(node: NodeGene) {
        nodes += node
        eligibleNodes(nodes, node).forEach {
            _connectableNodes.add(PotentialConnection(node.node, it.node))
        }
    }

    override fun connectionsTo(first: NodeGene): List<ConnectionGene> {
        return connections.filter { it.outNode == first.node }
    }

    override fun connectionsFrom(first: NodeGene): List<ConnectionGene> {
        return connections.filter { it.inNode == first.node }
    }

    override fun toString(): String {
        return "SimpleNeatMutator(_connectableNodes=$_connectableNodes, nodes=$nodes, connections=$connections)"
    }

}