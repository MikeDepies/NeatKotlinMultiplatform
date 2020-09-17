data class NetworkNode(val activationFunction: ActivationFunction, var value: Float, var activatedValue: Float)

fun NetworkNode.activate() {
    print("$value, ")
    activatedValue = this.activationFunction(value)
    value = 0f
}