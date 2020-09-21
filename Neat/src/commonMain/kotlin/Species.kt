//val Species?.notNull get() = null != this

class SpeciationController(
    private var speciesId: Int,
    private val compatibilityTest: (NeatMutator, NeatMutator) -> Boolean
) {
    private val neatMutatorToSpeciesMap = mutableMapOf<NeatMutator, Species>()
    private val speciesMap = mutableMapOf<Species, MutableSet<NeatMutator>>()
    fun nextSpecies(): Species = Species(speciesId++).also { speciesMap[it] = mutableSetOf() }
    fun speciate(population: List<NeatMutator>) {

        pruneSpeciesMap(population)
        population.forEach { neatMutator ->
            compatibleSpecies(neatMutator, compatibilityTest).let { addSpecies(neatMutator, it) }
        }

    }

    private fun pruneSpeciesMap(population: List<NeatMutator>) {
        neatMutatorToSpeciesMap.clear()
        speciesSet.forEach { species ->
            val newSpeciesPopulation = getSpeciesPopulation(species).intersect(population).toMutableSet()
            speciesMap[species] = newSpeciesPopulation
            newSpeciesPopulation.forEach { neatMutator ->
                neatMutatorToSpeciesMap[neatMutator] = species
            }
            if (newSpeciesPopulation.isEmpty()) {
                speciesMap.remove(species)
            }
        }
    }

    private fun addSpecies(
        neatMutator: NeatMutator,
        compatibleSpecies: Species?
    ): Species {
        val species = compatibleSpecies ?: nextSpecies()
        val speciesSet = speciesMap.getValue(species)
        speciesSet += neatMutator
        neatMutatorToSpeciesMap[neatMutator] = species
        return species
    }

    fun compatibleSpecies(neatMutator: NeatMutator, compatible: CompatibilityTest): Species? {
        return speciesMap.keys.firstOrNull {
            val avatarForSpecies = avatarForSpecies(it)
            avatarForSpecies != null && compatible(neatMutator, avatarForSpecies)
        }
    }

    fun avatarForSpecies(species: Species) = speciesMap[species]?.firstOrNull()

    val speciesSet: Set<Species> get() = speciesMap.keys.toSet()
    fun getSpeciesPopulation(species: Species) = speciesMap.getValue(species)
    fun sortSpeciesByFitness(fitnessForModelFn: (NeatMutator) -> Float) {
        speciesSet.forEach {
            speciesMap[it] = getSpeciesPopulation(it).sortedByDescending(fitnessForModelFn).toMutableSet()
        }
    }
}

typealias CompatibilityTest = (NeatMutator, NeatMutator) -> Boolean