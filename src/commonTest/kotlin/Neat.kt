import kotlin.test.*

class NeatTest {
    @Test
    fun `Add connection with node ids that aren't in neat mutator`() {
        val neat: NeatMutator = neatMutator(4, 2)
        val connectionGene = ConnectionGene(100, 101, 1f, true, 1)
        neat.apply {
            assertFails("Connection can not be added because connection gene refers to nodes that aren't in the neat mutator") {
                neat.addConnection(connectionGene)
            }
        }
        //new single connection gene added connecting two previously unconnected ndoes.
    }

    @Test
    fun `Add connection with source node id not in neat mutator`() {
        val neat: NeatMutator = neatMutator(4, 2)
        val connectionGene = ConnectionGene(100, 1, 1f, true, 1)
        neat.apply {
            assertFails("Connection can not be added because connection gene refers to nodes that aren't in the neat mutator") {
                neat.addConnection(connectionGene)
            }
        }
        //new single connection gene added connecting two previously unconnected ndoes.
    }

    @Test
    fun `Add connection with target node id not in neat mutator`() {
        val neat: NeatMutator = neatMutator(4, 2)
        val connectionGene = ConnectionGene(1, 100, 1f, true, 1)
        neat.apply {
            assertFails("Connection can not be added because connection gene refers to nodes that aren't in the neat mutator") {
                neat.addConnection(connectionGene)
            }
        }
        //new single connection gene added connecting two previously unconnected ndoes.
    }

    @Test
    fun `Add connection to neat mutator`() {
        val neat: NeatMutator = neatMutator(4, 2)
        val connectionGene = ConnectionGene(neat.inputNodes.first().node, neat.hiddenNodes.first().node, 1f, true, 1)
        neat.apply {
            neat.addConnection(connectionGene)
        }
        //new single connection gene added connecting two previously unconnected ndoes.
    }

    @Test
    fun `Neat Mutator has proper number of nodes on construction`() {
        val neatMutator = neatMutator(4, 2)
        assertEquals(4, neatMutator.inputNodes.size)
        assertEquals(2, neatMutator.outputNodes.size)
        assertEquals(1, neatMutator.hiddenNodes.size)
    }

    @Test
    fun `Neat Mutator has proper connection setup between nodes on construction`() {
        val neatMutator = neatMutator(4, 2)
        //assert input to hidden connections
        //assert hidden to output connections

        val connectionsToHiddenNode = neatMutator.connectionsTo(neatMutator.hiddenNodes.first())
        val connectionsFromHiddenNode = neatMutator.connectionsFrom(neatMutator.hiddenNodes.first())
        println(connectionsFromHiddenNode)
        println(connectionsToHiddenNode)
        assertEquals(4, connectionsToHiddenNode.size)
        assertEquals(2, connectionsFromHiddenNode.size)
        assertTrue(connectionsFromHiddenNode.all { it.enabled })
        assertTrue(connectionsToHiddenNode.all { it.enabled })
    }

    @Test
    fun `Node without connections`() {
        val neatMutator = neatMutator(2, 2)
        neatMutator.apply {
            val nodeId = lastNode.node + 1
            val node = NodeGene(nodeId, NodeType.Hidden)
            addNode(node)
            assertTrue(neatMutator.connectionsFrom(node).isEmpty())
            assertTrue(neatMutator.connectionsTo(node).isEmpty())
        }
    }


    fun mutateAddNode() {
        //an existing connection is split. New node placed where the old connection was.
        //Old connection is disabled
        //two new connections are created.
        //connection 1 leading into new node has a weight of 1.
        //connection 2 leaving the new node and connecting to the outgoing node has the weight of the old connection
    }
}