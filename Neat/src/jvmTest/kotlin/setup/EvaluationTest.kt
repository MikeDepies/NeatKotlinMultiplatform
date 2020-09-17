package setup

import ActivatableNetwork
import ComputationStrategy
import NeatMutator
import NetworkNode
import SimpleActivatableNetwork
import getComputationStrategy
import io.mockk.*
import neatMutator
import sigmoidalTransferFunction
import kotlin.random.*
import kotlin.test.*

class EvaluationTest {
    @Test
    fun `Cyclic connection causing a +1 sequence when input = 1`() {
        val random = mockk<Random>()
        every { random.nextFloat() } returnsMany listOf(1f)

        val neatMutator = initializeCyclicConnectionsNeatModel(random, outputActivation = sigmoidalTransferFunction)
        val input = listOf(1f)
        val network = neatMutator.toNetwork()
        repeat(5) {
            network.evaluate(input)

            val result = network.output()

            val expected = listOf(1f + it.toFloat())
//            assertEquals(expected, result)
            println("value=${network.outputNodes.map { it.value }}\nactivated=$result")
        }
//        neatMutator.evaluate(input)
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
        val b = mockk<Random>()
        every { b.nextFloat() } returnsMany listOf(1f)
//        b.nextFloat()

//        every { random.nextFloat() } returnsMany listOf(.3f, .6f)
        val network = neatMutator(1, 1, b).toNetwork()
        val input = listOf(2f)
        network.evaluate(input)
        val result = network.output()
//        val result = neatMutator.evaluate(input)
        assertEquals(expected, result)
    }

}

fun NeatMutator.toNetwork(): ActivatableNetwork {
    val idNodeMap = nodes.map { it.node to it }.toMap()
    val networkNodeMap = nodes.map { it to NetworkNode(it.activationFunction, 0f, 0f) }.toMap()
    val inputNodeSet = inputNodes.mapNotNull { networkNodeMap[it] }
    val outputNodeSet = outputNodes.map { networkNodeMap.getValue(it) }
    val computationStrategy: ComputationStrategy = getComputationStrategy(networkNodeMap, idNodeMap)

    return SimpleActivatableNetwork(inputNodeSet, outputNodeSet, computationStrategy)
}
