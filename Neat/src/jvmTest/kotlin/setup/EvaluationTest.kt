package setup

import NeatMutator
import io.mockk.*
import kotlin.random.*
import kotlin.test.*

class EvaluationTest {
    @Test
    fun `don't repeat node activations`() {
        val random = mockk<Random>()
        val neatMutator = initializeCyclicConnectionsNeatModel(random)
        neatMutator.evaluate()
        assertEquals(1f, neatMutator.outputNodes[0].value)
    }

    fun `initialized network evaluates`() {

    }

}

fun NeatMutator.evaluate(): List<Float> {
    return outputNodes.map { it.value }
}