package setup

interface ActivatableNetwork {
    fun evaluate(input: List<Float>) : Unit
    fun output(): List<Float>
}

