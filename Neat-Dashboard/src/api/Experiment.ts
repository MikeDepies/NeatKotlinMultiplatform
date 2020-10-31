export type ExperimentSession = {
  id: number
}

export type InitialPopulationRoute = {
  initialPopulation: OrganismDNA[]
}

export type OrganismDNA = {
  nodes: NodeDNA[]
  connections: ConnectionDNA[]
}

export type NodeDNA = {
  node: number,
  nodeType: string,
  activationFunction: string
}

export type ConnectionDNA = {
  inNode: number,
  outNode: number,
  weight: number,
  enabled: boolean,
  innovation: number,
}
