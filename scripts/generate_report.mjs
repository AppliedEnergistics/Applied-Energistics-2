import path from 'path';
import { readFileSync, writeFileSync } from 'fs';
import { URL, fileURLToPath } from 'url';
import { D3Node } from 'd3-node';
import * as d3 from 'd3';

const __dirname = fileURLToPath(new URL('.', import.meta.url));
const repositoryRoot = path.join(__dirname, '..');
let jmhDirectory = path.join(repositoryRoot, 'build/results/jmh/');

const allResults = JSON.parse(readFileSync(path.join(jmhDirectory, 'all-results.json')));

const hashes = [];
const commitMessages = [];
const multiSeries = {};
const firstCommit = allResults[allResults.length - 1];
var i = 0;
for (const run of firstCommit) {
    const arr = run.benchmark.split(".")
    const name = arr[arr.length - 1]
    multiSeries[name] = {
        benchmark: name,
        color: d3.schemeCategory10[i++],
        scalingFactor: run.primaryMetric.score,
        data: []
    }
}
for (const runsForCommit of allResults) {
    hashes.push(runsForCommit[0].hash.substring(0, 6));
    commitMessages.push(runsForCommit[0].message)
    for (const run of runsForCommit) {
        const arr = run.benchmark.split(".")
        const name = arr[arr.length - 1]
        const series = multiSeries[name];
        series.data.push({
            x: run.message,
            y: run.primaryMetric.score / series.scalingFactor,
            lci: run.primaryMetric.scoreConfidence[0] / series.scalingFactor,
            uci: run.primaryMetric.scoreConfidence[1] / series.scalingFactor,
        });
    }
}

const data = Object.values(multiSeries)

const width = 1280
const height = 720
const margin = {top: 50, right: 10, bottom: 30, left: 100}
const chartWidth = width + margin.right + margin.left
const chartHeight = height + margin.top + margin.bottom
const errorBarWidth = 10

const d3n = new D3Node()

const x = d3.scaleBand().domain(commitMessages).range([width, 0]).paddingOuter(0.5).paddingInner(1)
const y = d3.scaleLinear().domain([0, d3.max(data, series => d3.max(series.data, d => d.uci))]).range([height, 0]).nice()

const errorBar = (datum) => {
    return (context, size) => {
        return {
            draw(context, size) {
                const lci = y(datum.lci) - y(datum.y);
                const uci = y(datum.uci) - y(datum.y);

                context.moveTo(0, uci);
                context.lineTo(0, lci);
                context.moveTo(-size / 2, lci);
                context.lineTo(size / 2, lci);
                context.moveTo(-size / 2, uci);
                context.lineTo(size / 2, uci);
            }
        };
    };
}

const svg = d3n.createSVG(chartWidth, chartHeight)

const g = svg.append("g").attr("transform", `translate(${margin.left},${margin.top})`);
g.append("g").attr("transform", `translate(${width},0)`).attr("stroke-dasharray", "1 1").call(d3.axisLeft(y).tickSize(width));
g.append("g").attr("transform", `translate(0,${height - margin.bottom})`).call(d3.axisBottom(x));

g.append("text")
    .attr("x", 200)
    .attr("y", -10)
    .attr("font-size", 28)
    .text("Speedup factor, normalized to first commit, higher is better")

const d = g.selectAll("g.series")
    .data(data, series => series.benchmark)
    .join("g")
    .classed("series", true)
    .attr("stroke", series => series.color)
    .attr("fill", "none")
d.selectAll("path.line")
    .data(series => [series.data])
    .join("path")
    .classed("line", true)
    .attr("stroke-width", 3)
    .attr("d", d3.line().x((d) => x(d.x)).y((d) => y(d.y)));
d.selectAll("path.error-bar")
    .data(series => series.data)
    .join("path")
    .classed("error-bar", true)
    .attr("transform", (d) => `translate(${x(d.x)},${y(d.y)})`)
    .attr("stroke-width", 2)
    .attr("d", (d) => d3.symbol(errorBar(d)).size(errorBarWidth)())

const legendPosition = (d, i) => 88 + i * 32;
g.selectAll("mydots")
    .data(data)
    .enter()
    .append("circle")
    .attr("cx", 100)
    .attr("cy", legendPosition)
    .attr("r", 7)
    .style("fill", (d) => d.color)

g.selectAll("mylabels")
    .data(data)
    .enter()
    .append("text")
    .attr("x", 120)
    .attr("y", legendPosition)
    .style("fill", (d) => d.color)
    .text((d) => d.benchmark)
    .attr("text-anchor", "left")
    .style("alignment-baseline", "middle")

writeFileSync(path.join(jmhDirectory, 'chart.svg'), d3n.svgString())

