import kotlin.math.roundToInt

//val Species?.notNull get() = null != this

class SpeciationController(
    private var speciesId: Int,
    private val compatibilityTest: (NeatMutator, NeatMutator) -> Boolean
) {
    private val neatMutatorToSpeciesMap = mutableMapOf<NeatMutator, Species>()
    private val speciesMap = mutableMapOf<Species, MutableSet<NeatMutator>>()
    private fun nextSpecies(): Species = Species(speciesId++)
    fun speciate(population: List<NeatMutator>, speciesLineage: SpeciesLineage, generation: Int) {
        speciesMap.clear()
        neatMutatorToSpeciesMap.clear()
        population.forEach { neatMutator ->
            val compatibleSpecies = speciesLineage.compatibleSpecies(neatMutator, compatibilityTest)
            val species = if (compatibleSpecies != null) compatibleSpecies else {
                val nextSpecies = nextSpecies()
                speciesLineage.addSpecies(nextSpecies, generation, neatMutator)
                nextSpecies
            }
            if (!speciesMap.containsKey(species)) {
                speciesMap[species] = mutableSetOf()
            }
            addSpecies(neatMutator, species)
        }

    }

    fun avatarForSpecies(species: Species) = speciesMap.getValue(species).first()

    private fun addSpecies(
        neatMutator: NeatMutator,
        species: Species
    ): Species {

        val speciesSet = speciesMap.getValue(species)
        speciesSet += neatMutator
        neatMutatorToSpeciesMap[neatMutator] = species
        return species
    }

    val speciesSet: Set<Species> get() = speciesMap.keys.toSet()
    fun getSpeciesPopulation(species: Species) = speciesMap.getValue(species)
    fun sortSpeciesByFitness(fitnessForModelFn: (NeatMutator) -> Float) {
        speciesSet.forEach {
            speciesMap[it] = getSpeciesPopulation(it).sortedByDescending(fitnessForModelFn).toMutableSet()
        }
    }

    fun species(neatMutator: NeatMutator) = neatMutatorToSpeciesMap.getValue(neatMutator)




}

fun evaluatePopulation(
    population: List<NeatMutator>,
    inputOutput: List<EnvironmentEntryElement>
): List<FitnessModel<NeatMutator>> {
    return population.map { neatMutator ->
        val network = neatMutator.toNetwork()
        val score = inputOutput.map {
            network.evaluate(it.first, true)
            if (network.output().map { it.roundToInt().toFloat() } == it.second) 1f else 0f
        }.sum()
        FitnessModel(neatMutator, score)
    }
}
