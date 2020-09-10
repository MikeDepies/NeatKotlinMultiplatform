import kotlin.math.*
import kotlin.test.*

typealias ActivationFunction = (Float) -> Float

fun sigmoidalTransferFunction(x: Float): Float = 1.div(1 + exp(-4.9f * x))
val Identity: ActivationFunction = { it }
val sigmoidalTransferFunction: ActivationFunction = ::sigmoidalTransferFunction

class ActivationFunctionTest {
    @Test
    fun `modified sig function x approaches -infinity`() {
        assertEquals(.5f, sigmoidalTransferFunction(Float.MIN_VALUE))
    }

    @Test
    fun `modified sig function approaches +infinity`() {
        assertEquals(1f, sigmoidalTransferFunction(Float.MAX_VALUE))
    }

    @Test
    fun `identity function`() {
        assertEquals(1f, Identity(1f))
    }
}
