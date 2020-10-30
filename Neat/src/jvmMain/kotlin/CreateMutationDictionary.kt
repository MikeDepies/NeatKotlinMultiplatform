import neat.MutationEntry
import neat.chanceToMutate
import neat.mutation.Mutation

fun createMutationDictionary(
    mutationMap: Map<String, Mutation>,
    mutations: List<MutationDefinition>
): List<MutationEntry> {
    return mutations.map {
        it.percentChance chanceToMutate mutationMap.getValue(it.name)
    }
}