data class NetworkNode(val activationFunction: ActivationFunction, var value: Float, var activatedValue: Float)

fun NetworkNode.activate() {
    activatedValue = this.activationFunction(value)
    value = 0f
}