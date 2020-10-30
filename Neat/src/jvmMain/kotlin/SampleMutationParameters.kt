fun sampleMutationParameters(): List<MutationParameter> {
    return listOf(
        MutationParameter("mutateConnections", "Mutate Connections", "Mutate the connections nodes.", .8f, true),
        MutationParameter(
            "mutateAddNode",
            "Mutate Add Node",
            "A mutation that takes a random connection and splices a node inbetween the connection.",
            .4f, false
        ),
        MutationParameter(
            "mutateAddConnection",
            "Mutate Add Connection",
            "A mutation that connects two unconnected nodes.",
            .4f, true
        ),
        MutationParameter(
            "mutatePerturbBiasConnections",
            "Mutate Perturb Bias Connections",
            "A mutation that randomly nudges each bias connection a small amount.",
            .1f, true
        ),
        MutationParameter(
            "mutateToggleConnection",
            "Mutate Toggle Connection",
            "A mutation that selects a random connection and switches and toggles its enabled state.",
            .11f, true
        ),
        MutationParameter(
            "mutateNodeActivationFunction",
            "Mutate Activation Function",
            "A mutation that causes a random node to be selected and a (new) random mutation to be chosen.",
            .1f, true
        )
    )
}