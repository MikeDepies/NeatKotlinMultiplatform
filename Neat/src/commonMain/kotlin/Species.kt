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
            addSpecies(neatMutator, compatibleSpecies(neatMutator, compatibilityTest))
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

    fun Species.population() = getSpeciesPopulation(this)



    fun modelScoreMap(adjustedFitnessModels: List<ModelScore>): Map<NeatMutator, ModelScore> {
        return adjustedFitnessModels.map {
            it.neatMutator to it
        }.toMap()
    }

    fun mutatePopulation(
        adjustedPopulationScore: List<ModelScore>
    ) {
        val modelScoreMap = modelScoreMap(adjustedPopulationScore)
        val speciesMap = speciesMap(modelScoreMap)
        val speciesFitnessFunction = speciesTopFitness(speciesMap)


//        fun
        //fitness metric per species (top, average, etc)
        //compute expected number of offspring for each individual organism
        //  o -> o.adjustedFitness / populationAdjustedFitnessAverage
        //compute for species
        //check if speciesOffspringExpected is less than total organisms (for experiment[?])
        //      give the difference between expected and total organisms - in new "organisms" to that species to refill the population]
        //Handle where population gets killed of by stagnation + add age to species


        TODO("Not yet implemented")
    }

    fun evaluatePopulation(
        population: List<NeatMutator>,
        inputOutput: List<EnvironmentEntryElement>
    ): List<Pair<FitnessModel<NeatMutator>, ActivatableNetwork>> {
        return population.map { neatMutator ->
            val network = neatMutator.toNetwork()
            val score = inputOutput.map {
                network.evaluate(it.first, true)
                if (network.output() == it.second) 1f else 0f
            }.sum()
            FitnessModel(neatMutator, score) to network
        }
    }

    fun speciesReport(modelScoreList : List<ModelScore>, overallAverageFitness : Double) : SpeciesReport {
        val speciesOffspringMap = mutableMapOf<Species, Int>()
        val expectedOffspringMap =
            modelScoreList.map { it.neatMutator to it.adjustedFitness / overallAverageFitness.toFloat() }.toMap()
        fun totalOffspring(speciesSet: Set<Species>): Int {
            var skim = 0.0
            var totalOffspring = 0
            for (species in speciesSet) {
                val countOffspring = expectedOffspringMap.values.countOffspring(skim)
                skim = countOffspring.skim
                totalOffspring += countOffspring.offspring
                speciesOffspringMap[species] = countOffspring.offspring
            }
            return totalOffspring
        }

        fun SpeciationController.findBestSpecies(): Species {
            return speciesSet.maxByOrNull { speciesOffspringMap.getValue(it) }
                ?: speciesSet.first()
        }




        val totalOffspring = totalOffspring(speciesSet)
        if (totalOffspring < modelScoreList.size && totalOffspring + 1 == modelScoreList.size) {
            val species: Species = findBestSpecies()
            speciesOffspringMap[species] = speciesOffspringMap.getValue(species) + 1
        } else {
            //Java example code zeroes out all species fitness and then sets the best species to the previous population size

        }
        val modelScoreMap = modelScoreMap(modelScoreList)
        val speciesMap = speciesMap(modelScoreMap)
        return SpeciesReport(speciesOffspringMap.toMap(), overallAverageFitness, speciesMap)
    }


    private fun speciesMap(modelScoreMap: Map<NeatMutator, ModelScore>): SpeciesScoredMap {
        fun speciesPopulation(species: Species): List<ModelScore> {
            return getSpeciesPopulation(species).map { neatMutator ->
                modelScoreMap.getValue(neatMutator)
            }
        }

        return speciesSet.map { species ->
            species to speciesPopulation(species)
        }.toMap()
    }


}
data class Offspring(val offspring: Int, val skim: Double)
class SpeciesReport(val speciesOffspringMap: Map<Species, Int>, val overallAverageFitness: Double, val speciesMap : SpeciesScoredMap) {

}
class OffspringCounter(val skim : Double, val y1 : Float = 1f) {


    fun countOffspring(expectedOffsprings: Collection<Float>): Offspring {
        var offspring = 0
        var newSkim = skim
        expectedOffsprings.forEach { expectedOffSpring ->
            val nTemp = expectedOffSpring.div(y1).toInt()
            offspring += nTemp
            newSkim += expectedOffSpring - (nTemp * y1)
            if (newSkim >= 1f) {
                offspring += 1
                newSkim -= 1f
            }
        }
        return Offspring(offspring, newSkim)
    }
}

fun Collection<Float>.countOffspring(skim: Double, y1: Float = 1f): Offspring {
    var offspring = 0
    var newSkim = skim
    this.forEach { expectedOffSpring ->
        val expectedOffspringValue = expectedOffSpring
        val nTemp = expectedOffspringValue.div(y1).toInt()
        offspring += nTemp
        newSkim += expectedOffspringValue - (nTemp * y1)
        if (newSkim >= 1f) {
            offspring += 1
            newSkim -= 1f
        }
    }
    return Offspring(offspring, newSkim)
}


typealias ExpectedOffSpring = Pair<NeatMutator, Float>

data class ModelScore(val neatMutator: NeatMutator, val fitness: Float, val adjustedFitness: Float)
data class Species(val id: Int, var age: Generations = 0)
typealias CompatibilityTest = (NeatMutator, NeatMutator) -> Boolean
typealias SpeciesScoredMap = Map<Species, Collection<ModelScore>>
typealias SpeciesMap = Map<Species, Collection<NeatMutator>>
typealias SpeciesFitnessFunction = (Species) -> Float

fun speciesAverageFitness(speciesScoredMap: SpeciesScoredMap): SpeciesFitnessFunction =
    { species -> speciesScoredMap.getValue(species).map { it.adjustedFitness }.average().toFloat() }

fun speciesTopFitness(speciesScoredMap: SpeciesScoredMap): SpeciesFitnessFunction =
    { species -> speciesScoredMap.getValue(species).first().adjustedFitness }