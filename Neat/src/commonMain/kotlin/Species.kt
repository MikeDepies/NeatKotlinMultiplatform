data class Species(val id: Int)


data class Offspring(val offspring: Int, val skim: Double)
class SpeciesReport(
    val speciesOffspringMap: Map<Species, Int>,
    val overallAverageFitness: Double,
    val speciesMap: SpeciesScoredMap
)

class OffspringCounter(val skim: Double, val y1: Float = 1f) {
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
typealias CompatibilityTest = (NeatMutator, NeatMutator) -> Boolean
typealias SpeciesScoredMap = Map<Species, Collection<ModelScore>>
typealias SpeciesMap = Map<Species, Collection<NeatMutator>>
typealias SpeciesFitnessFunction = (Species) -> Float

fun speciesAverageFitness(speciesScoredMap: SpeciesScoredMap): SpeciesFitnessFunction =
    { species -> speciesScoredMap.getValue(species).map { it.adjustedFitness }.average().toFloat() }

fun speciesTopFitness(speciesScoredMap: SpeciesScoredMap): SpeciesFitnessFunction =
    { species -> speciesScoredMap.getValue(species).first().adjustedFitness }

typealias OffspringReportFunction = SpeciationController.(List<ModelScore>) -> SpeciesReport
fun SpeciationController.calculateSpeciesReport(
    modelScoreList: List<ModelScore>,
    overallAverageFitness: Double
): SpeciesReport {

    val speciesOffspringMap = mutableMapOf<Species, Int>()
    val expectedOffspringMap =
        modelScoreList.map { it.neatMutator to it.adjustedFitness / overallAverageFitness.toFloat() }.toMap()
    var skim = 0.0
    var totalOffspring = 0
    for (species in speciesSet) {
        val countOffspring = expectedOffspringMap.values.countOffspring(skim)
        skim = countOffspring.skim
        totalOffspring += countOffspring.offspring
        speciesOffspringMap[species] = countOffspring.offspring
    }

    return SpeciesReport(
        speciesOffspringMap.toMap(),
        overallAverageFitness,
        speciesMap(modelScoreList.toMap { it.neatMutator })
    )
}

private fun SpeciationController.speciesMap(modelScoreMap: Map<NeatMutator, ModelScore>): SpeciesScoredMap {
    fun speciesPopulation(species: Species): List<ModelScore> {
        return getSpeciesPopulation(species).map { neatMutator ->
            modelScoreMap.getValue(neatMutator)
        }
    }

    return speciesSet.map { species ->
        species to speciesPopulation(species)
    }.toMap()
}


class SpeciesOffspringCalculator(
    val expectedOffspringMap: Map<NeatMutator, Float>,
    val speciesOffspringMap: MutableMap<Species, Int>
) {
    fun getOffspring(neatMutator: NeatMutator) = expectedOffspringMap.getValue(neatMutator)
    fun getSpeciesOffspring(species: Species) = speciesOffspringMap.getValue(species)
}

fun SpeciesOffspringCalculator.totalOffspring(speciesSet: Set<Species>): Int {
    return totalOffspring(
        speciesSet,
        expectedOffspringMap,
        speciesOffspringMap
    )
}

fun totalOffspring(
    speciesSet: Set<Species>,
    expectedOffspringMap: Map<NeatMutator, Float>,
    speciesOffspringMap: MutableMap<Species, Int>
): Int {
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