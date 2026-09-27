// Renders build/reports/jmh/all-results.json (written by run_jmh_tests.mjs) as a chart to build/reports/jmh/chart.svg.
// Each benchmark score is normalized to the first commit that has a result for that benchmark.
import path from 'path';
import { readFileSync, writeFileSync } from 'fs';
import { URL, fileURLToPath } from 'url';
import { D3Node } from 'd3-node';
import * as d3 from 'd3';

const __dirname = fileURLToPath(new URL('.', import.meta.url));
const repositoryRoot = path.join(__dirname, '..');
const jmhDirectory = path.join(repositoryRoot, 'build/reports/jmh/');

const allResults = JSON.parse(readFileSync(path.join(jmhDirectory, 'all-results.json'), 'utf8'));
if (allResults.length === 0) {
    console.error('all-results.json contains no results');
    process.exit(1);
}

// Shows "Class.method" rather than the fully qualified benchmark name
function benchmarkLabel(benchmark) {
    return benchmark.split('.').slice(-2).join('.');
}

function commitLabel(commit) {
    const name = commit.ref ?? commit.hash.substring(0, 8);
    const subject = commit.message.length > 40 ? commit.message.substring(0, 39) + '…' : commit.message;
    return `${name} ${subject}`;
}

// Collect all benchmarks from all commits, since benchmarks may be added or removed along the way
const benchmarks = [...new Set(allResults.flatMap(entry => entry.results.map(run => run.benchmark)))].sort();

const data = benchmarks.map((benchmark, seriesIndex) => {
    let scalingFactor;
    const points = allResults.map((entry, commitIndex) => {
        const run = entry.results.find(run => run.benchmark === benchmark);
        if (!run) {
            return { commitIndex, missing: true };
        }
        // The first commit with a result for this benchmark serves as its baseline
        scalingFactor ??= run.primaryMetric.score;
        const [lower, upper] = run.primaryMetric.scoreConfidence;
        return {
            commitIndex,
            missing: false,
            y: run.primaryMetric.score / scalingFactor,
            // JMH reports NaN confidence intervals if there is only a single iteration
            lci: (Number.isFinite(lower) ? lower : run.primaryMetric.score) / scalingFactor,
            uci: (Number.isFinite(upper) ? upper : run.primaryMetric.score) / scalingFactor,
        };
    });
    return {
        benchmark: benchmarkLabel(benchmark),
        color: d3.schemeCategory10[seriesIndex % d3.schemeCategory10.length],
        data: points,
    };
});

const width = 1280
const height = 720
const margin = {top: 50, right: 40, bottom: 200, left: 100}
const chartWidth = width + margin.right + margin.left
const chartHeight = height + margin.top + margin.bottom
const errorBarWidth = 10

const d3n = new D3Node()

// Commits are keyed by their index, since the same commit may be benchmarked more than once
const commitLabels = allResults.map(entry => commitLabel(entry.commit));
const x = d3.scaleBand().domain(d3.range(allResults.length)).range([0, width]).paddingOuter(0.5).paddingInner(1)
const presentPoints = data.flatMap(series => series.data.filter(d => !d.missing));
const y = d3.scaleLinear().domain([0, d3.max(presentPoints, d => d.uci)]).range([height, 0]).nice()

const errorBar = (datum) => {
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
}

const svg = d3n.createSVG(chartWidth, chartHeight)

const g = svg.append("g").attr("transform", `translate(${margin.left},${margin.top})`);
g.append("g").attr("transform", `translate(${width},0)`).attr("stroke-dasharray", "1 1").call(d3.axisLeft(y).tickSize(width));
g.append("g")
    .attr("transform", `translate(0,${height})`)
    .call(d3.axisBottom(x).tickFormat(i => commitLabels[i]))
    .selectAll("text")
    .attr("text-anchor", "end")
    .attr("transform", "rotate(-35)")
    .attr("dx", "-0.5em")
    .attr("dy", "0.5em");

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
    .attr("d", d3.line().defined(d => !d.missing).x(d => x(d.commitIndex)).y(d => y(d.y)));
d.selectAll("path.error-bar")
    .data(series => series.data.filter(d => !d.missing))
    .join("path")
    .classed("error-bar", true)
    .attr("transform", (d) => `translate(${x(d.commitIndex)},${y(d.y)})`)
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
