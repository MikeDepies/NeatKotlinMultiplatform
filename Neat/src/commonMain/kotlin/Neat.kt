import kotlin.random.Random

typealias Generations = Int

typealias EnvironmentQuery = () -> EnvironmentEntryElement
typealias EnvironmentEntryElement = Pair<List<Float>, List<Float>>

data class FitnessModel<T>(val model: T, val score: Float)

fun <T> identity(): (T) -> T = { it }

fun neatMutator(
    inputNumber: Int,
    outputNumber: Int,
    random: Random = Random,
    function: ActivationFunction = Identity
): NeatMutator {
    val simpleNeatMutator = simpleNeatMutator(listOf(), listOf())
    var nodeNumber = 0
    var innovation = 0
    repeat(inputNumber) {
        simpleNeatMutator.addNode(NodeGene(nodeNumber++, NodeType.Input, identity()))
    }
//    val hiddenNode = NodeGene(nodeNumber++, NodeType.Hidden)
//    simpleNeatMutator.addNode(hiddenNode)
    repeat(outputNumber) {
        simpleNeatMutator.addNode(NodeGene(nodeNumber++, NodeType.Output, function))
    }
    for (input in simpleNeatMutator.inputNodes) {
//        val weight = random.nextFloat();
        for (output in simpleNeatMutator.outputNodes) {
            val weight = random.nextFloat()
            simpleNeatMutator.addConnection(
                ConnectionGene(
                    input.node,
                    output.node,
                    weight,
                    true,
                    innovation++
                )
            )
        }
    }

    return simpleNeatMutator
}

