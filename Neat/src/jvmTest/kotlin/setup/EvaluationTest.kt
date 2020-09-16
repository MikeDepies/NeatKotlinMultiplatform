package setup

import ActivationFunction
import NeatMutator
import NodeGene
import io.mockk.*
import neatMutator
import sigmoidalTransferFunction
import kotlin.random.*
import kotlin.test.*

class EvaluationTest {
    //    @Test
    fun `don't repeat node activations`() {
        val random = mockk<Random>()
        val neatMutator = initializeCyclicConnectionsNeatModel(random)
        val input = listOf(1f)
        neatMutator.evaluate(input)
//        assertEquals(1f, neatMutator.outputNodes[0].value)
    }

    @Test
    fun `evaluate network (1,1) with weights (1, 1) input 1f`() {
        val expected = listOf(1f)
        val random = mockk<Random>()
        every { random.nextFloat() } returnsMany listOf(1f, 1f)
        val network = neatMutator(1, 1, random).toNetwork()
        val input = listOf(1f)
        network.evaluate(input)
        val result = network.output()
//        val result = neatMutator.evaluate(input)
        assertEquals(expected, result)
    }

    @Test
    fun `evaluate network (1,1) with weights (,1, ,1) input 1f Output Fn=Identity`() {
        val expected = listOf(.1f)
        val random = mockk<Random>()
        every { random.nextFloat() } returnsMany listOf(.1f)
        val network = neatMutator(1, 1, random).toNetwork()
        val input = listOf(1f)
        network.evaluate(input)
        val result = network.output()
//        val result = neatMutator.evaluate(input)
        assertEquals(expected, result)
    }

    @Test
    fun `evaluate network (1,1) with weights (,1, ,1) input 1f Output Fn=Sigmoidal`() {
        val expected = listOf(.5f)
        val random = mockk<Random>()
        every { random.nextFloat() } returnsMany listOf(.1f)
        val network = neatMutator(1, 1, random, sigmoidalTransferFunction).toNetwork()
        val input = listOf(0f)
        network.evaluate(input)
        val result = network.output()
//        val result = neatMutator.evaluate(input)
        assertEquals(expected, result)
    }

    @Test
    fun `evaluate network (1,1) with weights (1, 1) input 2`() {
        val expected = listOf(2f)
        val random = mockk<Random>()
        every { random.nextFloat() } returnsMany listOf(1f, 1f)
        val network = neatMutator(1, 1, random).toNetwork()
        val input = listOf(2f)
        network.evaluate(input)
        val result = network.output()
//        val result = neatMutator.evaluate(input)
        assertEquals(expected, result)
    }

}

fun NeatMutator.evaluate(input: List<Float>): List<Float> {
    val nodeActivationSet = mutableSetOf<NodeGene>()
//    inputNodes.map { connectionsFrom(it).map {  } }
//    return outputNodes.map { it.value }
    TODO()
}

data class NetworkNode(val activationFunction: ActivationFunction, var value: Float)

fun NetworkNode.activate() {
    value = this.activationFunction(value)
}
typealias ComputationStrategy = NeatMutator.() -> Unit

fun Set<NodeGene>.activate(map: Map<NodeGene, NetworkNode>) = forEach { map.getValue(it).activate() }
fun NeatMutator.toNetwork(): ActivatableNetwork {
    val idNodeMap = nodes.map { it.node to it }.toMap()
    val networkNodeMap = nodes.map { it to NetworkNode(it.activationFunction, 0f) }.toMap()
    val nodeMap = nodes.map { networkNodeMap.getValue(it) to it }.toMap()
    val inputNodeSet = inputNodes.mapNotNull { networkNodeMap[it] }
    val outputNodeSet = outputNodes.map { networkNodeMap.getValue(it) }
    fun applyInputValues(inValues: List<Float>) {
        inValues.indices.forEach { inputNodeSet[it].value = inValues[it] }
    }

    val computationStrategy: NeatMutator.() -> Unit = getComputationStrategy(networkNodeMap, idNodeMap)
    return object : ActivatableNetwork {
        override fun evaluate(input: List<Float>) {
            applyInputValues(input)
            computationStrategy()
        }

        override fun output(): List<Float> {
            return outputNodeSet.map { it.value }
        }

    }

}

private fun NeatMutator.getComputationStrategy(
    networkNodeMap: Map<NodeGene, NetworkNode>,
    idNodeMap: Map<Int, NodeGene>
): NeatMutator.() -> Unit {
    val computationSequence = computationSequence(networkNodeMap, idNodeMap)
    val outputNodeSet = outputNodes.map { networkNodeMap.getValue(it) }
    return {
        computationSequence.forEach { it() }
        outputNodeSet.forEach { it.activate() }
    }

}

fun NeatMutator.computationSequence(
    networkNodeMap: Map<NodeGene, NetworkNode>,
    idNodeMap: Map<Int, NodeGene>
): Sequence<() -> Unit> {
    return sequence {
        val activationSet = mutableSetOf<NetworkNode>()
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
                    outputNode.value += inputNode.value * connection.weight
                }


            }
            activeSet.map { networkNodeMap.getValue(it) }.forEach { activationSet += it }
            activeSet = nextNodeMap.keys
            yield(fn)
        }
    }
}

class NetworkTest {
    fun `transform into activatable network`() {

    }
}