import kotlin.random.Random

/**
 * Random/configuration part of the neat algorithm. This will utilize a Mutator that can peform the desire operation,
 * where as the experiment decides on the type of operations
 */
interface NeatExperiment {
    val random: Random
    fun mutateAddConnection(neatMutator: NeatMutator)
    fun mutateAddNode(neatMutator: NeatMutator)
    fun nextInnovation(): Int
    fun crossover(parent1: FitnessModel<NeatMutator>, parent2: FitnessModel<NeatMutator>): NeatMutator
    fun nextNode(): Int
}