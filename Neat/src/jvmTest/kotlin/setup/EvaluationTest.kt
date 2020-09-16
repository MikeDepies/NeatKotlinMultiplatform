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


fun NeatMutator.toNetwork(): ActivatableNetwork {
    val idNodeMap = nodes.map { it.node to it }.toMap()
    val networkNodeMap = nodes.map { it to NetworkNode(it.activationFunction, 0f) }.toMap()
    val nodeMap = nodes.map { networkNodeMap.getValue(it) to it }.toMap()
    val inputNodeSet = inputNodes.mapNotNull { networkNodeMap[it] }
    val outputNodeSet = outputNodes.map { networkNodeMap.getValue(it) }
    fun Set<NodeGene>.activate() = forEach { networkNodeMap.getValue(it).activate() }

    fun rollForward(): (List<Float>) -> Unit {
        var activeSet = inputNodes.toSet()
        val activationSet = mutableSetOf<NetworkNode>()
        fun networkNotFullyActivated() = (activationSet.size + outputNodes.size) < nodes.size
        val computationSet = sequence<() -> Unit> {
            while (networkNotFullyActivated()) {
                val capturedSet = activeSet
                val connections = capturedSet.flatMap { node ->
                    connectionsFrom(node)
                }

                val nextNodeMap = connections.groupBy { idNodeMap.getValue(it.outNode) }
                val fn = {
//
                    capturedSet.activate()
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
        return { input: List<Float> ->
            input.indices.forEach {
                inputNodeSet[it].value = input[it]
            }
            computationSet.forEach {
                it()
            }
            outputNodeSet.forEach {
                it.activate()
            }
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