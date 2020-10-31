<script>
	import { wsListen } from "./store/Websocket";
	import { parameters } from "./store/EnvironmentStore";
	import type { XorParameters } from "./api/Environment";
	import SetupEnvironment from "@components/SetupEnvironment.svelte";
	import { activeExperiment, startExperiment } from "./store/ExperimentStore";
	import type {
		ConnectionDNA,
		InitialPopulationRoute,
		NodeDNA,
		OrganismDNA,
	} from "@app/api/Experiment";
	import {
		forceCenter,
		forceLink,
		forceManyBody,
		forceSimulation,
	} from "d3-force";

	import type {
		Simulation,
		SimulationLinkDatum,
		SimulationNodeDatum,
	} from "d3-force";
	import Network from "@components/Network.svelte";

	type RegisterSessionRoute = {
		register: boolean;
	};
	type StartExperimentRoute = {
		start: string;
	};
	type NewGenerationRoute = {
		newGeneration: NewGeneration;
	};
	type NewGeneration = {
		generation: number;
		organisms: { speciesId: number; dna: OrganismDNA }[];
	};
	let { write, read, readWithDefault } = wsListen<
		StartExperimentRoute &
			RegisterSessionRoute &
			InitialPopulationRoute &
			NewGenerationRoute
	>();
	let currentGen = 0;
	let snycGenUpdates = true;
	let start = read("start");
	let register = read("register");
	let initialPopulation = read("initialPopulation");
	let newGen = read("newGeneration");
	let width = 300,
		height = 200;

	type SimpleSimulation = {
		links: SimulationLinkDatum<SimulationNodeDatum>;
		nodes: SimulationNodeDatum[];
	};
	let generationHistory: NewGeneration[] = [];
	// $: {
	// 	const p = $initialPopulation;
	// 	if (p && p.length > 0) {
	// 		generationHistory = [
	// 			...generationHistory,
	// 			{
	// 				generation: 0,
	// 				organisms: p.map((o) => ({
	// 					speciesId: -1,
	// 					dna: { ...o },
	// 				})),
	// 			},
	// 		];
	// 	}
	// }
	let currentGeneration: NewGeneration | undefined;
	$: headOfHistory =
		generationHistory && generationHistory.length > 0
			? generationHistory[generationHistory.length - 1]
			: undefined;

	$: console.log(headOfHistory);
	$: {
		console.log(generationHistory);
		if (snycGenUpdates && headOfHistory) currentGen = headOfHistory.generation + 0;
		currentGeneration =
			generationHistory && generationHistory.length > 0
				? generationHistory[currentGen]
				: undefined;
	}

	$: {
		const g = $newGen;
		if (g && !generationHistory.includes(g)) {
			console.log("new gen!");
			generationHistory = [...generationHistory, g];
		}
	}
	function buildExperimentConfig(): XorParameters {
		return {
			generations: $parameters.generations,
			mateChance: $parameters.mateChance,
			populationSize: $parameters.populationSize,
			sharingThreshold: $parameters.sharingThreshold,
			survivalThreshold: $parameters.survivalThreshold,
			activationFunctions: $parameters.activationFunctions
				.filter((activation) => activation.enabled)
				.map((activation) => ({ name: activation.name })),
			mutations: $parameters.mutations
				.filter((mutation) => mutation.enabled)
				.map((mutation) => ({
					name: mutation.name,
					percentChance: mutation.percentChance,
				})),
		};
	}
	async function startSimulation() {
		let response = await fetch("http://localhost:8090/start", {
			headers: {
				Accept: "application/json",
				"Content-Type": "application/json",
			},
			method: "POST",
			body: JSON.stringify(buildExperimentConfig()),
		});
		let sessionId: { sessionId: number } = await response.json();
		write("register", sessionId);
		startExperiment(sessionId);
		// parameters = await response.json();
	}
	function turnOffSync() {
		console.log("test " + generationHistory);
		snycGenUpdates = false;
	}
	let environments = ["XOR"];
</script>

<div>{currentGen}</div>
{#if currentGeneration}
	<div on:mouseenter={turnOffSync}>
		<input
			type="range"
			min={0}
			max={generationHistory.length - 1}
			bind:value={currentGen} />
	</div>
{/if}
<div class="max-w-7xl mx-auto sm:px-6 lg:px-8">
	<div class="max-w-3xl mx-auto">
		<!-- { $parameters.populationSize } -->
		{#if $activeExperiment && currentGeneration}
			<div class="bg-green-100 flex flex-wrap justify-center">
				{#each currentGeneration.organisms as organism}
					<!-- {@debug organism} -->
					<Network organism={organism.dna} />
				{/each}
			</div>
		{:else}
			<SetupEnvironment
				{environments}
				parameters={$parameters}
				on:startSimulation={startSimulation} />
		{/if}
	</div>
</div>
