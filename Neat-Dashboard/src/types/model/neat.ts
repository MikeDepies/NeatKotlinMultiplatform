export type NodeType = "Input" | "Hidden" | "Output";
export type ActivationFunction = "Identity" | "Sigmoidal";
export interface NodeGene {
    node: number;
    type: NodeType;
    activation: ActivationFunction;
  }
export interface ConnectionGene {
    innovation: number;
    weight: number;
    enabled: boolean;
    inNode: number;
    outNode: number;
  }

export interface NeatMutator {
    nodes: NodeGene[];
    connections: ConnectionGene[];
  }
