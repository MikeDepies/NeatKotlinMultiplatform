class SimpleActivatableNetwork(
    private val inputNodes: List<NetworkNode>,
    private val outputNodes: List<NetworkNode>,
    val computationStrategy: ComputationStrategy
) : ActivatableNetwork {
    private fun applyInputValues(inValues: List<Float>) {
        inValues.indices.forEach { inputNodes[it].value = inValues[it] }
    }

    override fun evaluate(input: List<Float>) {
        applyInputValues(input)
        computationStrategy()
    }

    override fun output(): List<Float> {
        return outputNodes.map { it.activatedValue }
    }

}