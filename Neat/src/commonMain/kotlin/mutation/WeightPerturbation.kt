package mutation

import NeatExperiment

fun NeatExperiment.weightPerturbation(range: Float) = (random.nextFloat() * (range * 2)) - range