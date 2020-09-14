package setup

import NeatMutator
import NodeGene
import io.mockk.*
import neatMutator
import kotlin.random.*
import kotlin.test.*

class EvaluationTest {
    @Test
    fun `don't repeat node activations`() {
        val random = mockk<Random>()
        val neatMutator = initializeCyclicConnectionsNeatModel(random)
        val input = listOf(1f)
        neatMutator.evaluate(input)
//        assertEquals(1f, neatMutator.outputNodes[0].value)
    }

    @Test
    fun `initialized network evaluates`() {
        val expected = listOf(1f)
        val random = mockk<Random>()
        every { random.nextFloat() } returnsMany listOf(.2f)
        val neatMutator = neatMutator(1, 1, random)
        val input = listOf(1f)
        val result = neatMutator.evaluate(input)
        assertEquals(expected, result)
    }

}

fun NeatMutator.evaluate(input: List<Float>): List<Float> {
    val nodeActivationSet = mutableSetOf<NodeGene>()
//    inputNodes.map { connectionsFrom(it).map {  } }
//    return outputNodes.map { it.value }
    TODO()
}

interface ActivatableNetwork {
}

fun NeatMutator.toNetwork() : ActivatableNetwork {
    TODO()
}

class NetworkTest {
    fun `transform into activatable network`() {

    }
}