<script>
  import type {
    OrganismDNA,
    NodeDNA,
    ConnectionDNA,
  } from "@app/api/Experiment";
  import {
    forceCenter,
    forceLink,
    forceManyBody,
    forceSimulation,
  } from "d3-force";
  import type { SimulationNodeDatum, SimulationLinkDatum } from "d3-force";
  import { tick } from "svelte";

  export let organism: OrganismDNA;
  let links: SimulationLinkDatum<ConnectionDNA & SimulationNodeDatum>[];
  $: {
    const org = organism;
    // console.log(org);
    links = org.connections.map((o) => ({
      ...o,
      source: o.inNode + "",
      target: o.outNode + "",
    }));
  }
  $: nodes = organism.nodes.map((o) => ({
    ...o,
    // index: o.node+"",
  }));
  let simulation = forceSimulation<
    NodeDNA & SimulationNodeDatum,
    ConnectionDNA & SimulationLinkDatum<NodeDNA & SimulationNodeDatum>
  >().on("tick", () => {
      nodes = [...nodes];
      links = [...links];
      // tick();
    });;
  $: simulation
    .nodes(nodes)
    .force("charge", forceManyBody().strength(-30))
    .force("center", forceCenter(300 / 2, 200 / 2))
    .force(
      "link",
      forceLink(links).id((a, b, c) => {
        let l = a as NodeDNA & SimulationNodeDatum;
        return "" + l.node;
      })
    )
    
</script>

<!-- test -->
<svg width="300" height="200" class="bg-green-400 block m-4">
  {#each links as link}
    <!-- {@debug link} -->
    <line
      stroke={(link.weight > 0) ? "black" : "red"}
      x1={link.source.x}
      y1={link.source.y}
      x2={link.target.x}
      y2={link.target.y} 
      stroke-width={5 * link.weight}/>
  {/each}
  {#each nodes as node, i}
    <circle fill={(node.nodeType == "Hidden" ) ? "green" : "black"} r="4" cx={node.x} cy={node.y} />
  {/each}
</svg>
