import kotlin.math.*
import kotlin.test.*

class ActivationFunctionTest {
    @Test
    fun `modified sig function x approaches -infinity`() {
        assertEquals(.5f, sigmoidalTransferFunction(Float.MIN_VALUE))
    }

    @Test
    fun `modified sig function approaches +infinity`() {
        assertEquals(1f, sigmoidalTransferFunction(Float.MAX_VALUE))
    }
}
typealias ActivationFunction = (Float) -> Float
fun sigmoidalTransferFunction(x: Float): Float = 1.div(1 + exp(-4.9f * x))
