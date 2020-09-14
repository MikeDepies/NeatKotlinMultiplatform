package setup

import ActivationFunction
import NeatMutator
import NodeGene
import io.mockk.*
import neatMutator
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
    fun `evaluate network (1,1) with weights (,1, ,1) input 1f`() {
        val expected = listOf(.001f)
        val random = mockk<Random>()
        every { random.nextFloat() } returnsMany listOf(.1f, .1f)
        val network = neatMutator(1, 1, random).toNetwork()
        val input = listOf(1f)
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

fun NeatMutator.toNetwork(): ActivatableNetwork {
    val idNodeMap = nodes.map { it.node to it }.toMap()
    val networkNodeMap = nodes.map { it to NetworkNode(it.activationFunction, 0f) }.toMap()
    val nodeMap = nodes.map { networkNodeMap.getValue(it) to it }.toMap()
    val inputNodeSet = inputNodes.mapNotNull { networkNodeMap[it] }
    val outputNodeSet = outputNodes.map { networkNodeMap.getValue(it) }
    fun rollForward(): (List<Float>) -> Unit {
        var activeSet = inputNodes.toSet()
        val activationSet = mutableSetOf<NetworkNode>()
        val computationSet = sequence<() -> Unit> {
            while (activationSet.size < nodes.size) {
                val connections = activeSet.flatMap { node ->
                    connectionsFrom(node)
                }
                val nextNodeMap = connections.groupBy { idNodeMap.getValue(it.outNode) }
                val fn = {
                    connections.forEach { connection ->
                        val inputValue = idNodeMap.getValue(connection.inNode)
                        val outValue = idNodeMap.getValue(connection.outNode)
                        val outputNode = networkNodeMap.getValue(outValue)
                        val inputNode = networkNodeMap.getValue(inputValue)
                        outputNode.value += inputNode.value * connection.weight
                    }
                    activeSet = nextNodeMap.keys
                    activeSet.forEach {
                        val node = networkNodeMap.getValue(it)
                        node.value = node.activationFunction(node.value)
                    }
                }
                yield(fn)
            }
        }.toList()
        return { input: List<Float> ->
            input.indices.forEach {
                inputNodeSet[it].value = input[it]
                activationSet += inputNodeSet[it]
            }
            computationSet.forEach { it() }


//            sequence<List<NetworkNode>> { }

        }
    }

    val calc = rollForward()
    return object : ActivatableNetwork {
        override fun evaluate(input: List<Float>) {
//            for (index in input.indices) {
//                outputNodeSet[index].value = input[index]
//            }
//            outputNodeSet.forEach { it.value = 1f }
            calc(input)
        }

        override fun output(): List<Float> {
            return outputNodeSet.map { it.value }
        }

    }
}

class NetworkTest {
    fun `transform into activatable network`() {

    }
}