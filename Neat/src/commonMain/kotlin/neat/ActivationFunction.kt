package neat

import kotlin.js.JsExport
import kotlin.math.*

typealias ActivationFunction = (Float) -> Float
@JsExport
class ActivationGene(val name : String, val activationFunction: ActivationFunction)
fun sigmoidalTransferFunction(x: Float): Float = 1.div(1 + exp(-4.9f * x))
val Identity: ActivationFunction = { it }
val SigmoidalTransferFunction: ActivationFunction = ::sigmoidalTransferFunction

fun baseActivationFunctions(): List<ActivationGene> {
    return listOf(
        ActivationGene("identity", identity()),
        ActivationGene("sigmoidal", SigmoidalTransferFunction),
    )
}

object Activation {
    val identity = ActivationGene("identity", identity())
    val sigmoidal = ActivationGene("sigmoidal", SigmoidalTransferFunction)
}