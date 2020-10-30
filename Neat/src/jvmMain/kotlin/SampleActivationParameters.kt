import neat.toMap

fun sampleActivationParameters(): Map<String, ActivationFunctionParameter> {
    return listOf(
        ActivationFunctionParameter(
            "identity",
            "Identity",
            "A simple Identity function - acts as a stand in for no transformation",
            true
        ), ActivationFunctionParameter(
            "sigmoidal",
            "Sigmoidal",
            "A Sigmoid function of the form: 1 / (1 + e^(-4.9 * x)",
            true
        )
    ).toMap { it.name }
}