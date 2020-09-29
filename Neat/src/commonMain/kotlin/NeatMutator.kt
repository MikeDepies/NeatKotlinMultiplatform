interface NeatMutator {
    val nodes: List<NodeGene>
    val connections: List<ConnectionGene>
    val hiddenNodes: List<NodeGene>
    val outputNodes: List<NodeGene>
    val inputNodes: List<NodeGene>
    val connectableNodes: List<PotentialConnection>

    val lastNode: NodeGene
    fun addConnection(connectionGene: ConnectionGene)
    fun addNode(node: NodeGene)
    fun connectionsTo(first: NodeGene): List<ConnectionGene>
    fun connectionsFrom(first: NodeGene): List<ConnectionGene>
    fun clone() : NeatMutator
}