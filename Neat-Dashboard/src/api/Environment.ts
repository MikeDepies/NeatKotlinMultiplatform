export type BasicParameters = {
  generations: number
  populationSize: number
  sharingThreshold: number
  mateChance: number
  survivalThreshold: number
}

export interface MutationParameter {
  name: string
  percentChance: number
}
export type ActivationFunctionParameter = {
  name: string
}

export type UI<T> = T & {
  displayName: string
  description: string
  enabled: boolean
}
export type XorParameters = BasicParameters & {
  activationFunctions: ActivationFunctionParameter[]
  mutations: MutationParameter[]
}
export type XorParametersUI = BasicParameters & {
  activationFunctions: UI<ActivationFunctionParameter>[]
  mutations: UI<MutationParameter>[]
}