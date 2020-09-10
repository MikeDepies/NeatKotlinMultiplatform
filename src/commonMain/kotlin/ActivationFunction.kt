import kotlin.math.*

typealias ActivationFunction = (Float) -> Float
fun sigmoidalTransferFunction(x: Float): Float = 1.div(1 + exp(-4.9f * x))
val Identity: ActivationFunction = { it }
val sigmoidalTransferFunction: ActivationFunction = ::sigmoidalTransferFunction