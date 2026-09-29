// Compares the benchmark results in build/reports/jmh/ (written by the jmh task, i.e. through run_jmh_tests.mjs) and
// renders them as a chart to build/reports/jmh/chart.svg. A table with the scores is printed as well.
//
// Usage:
//   node generate_report.mjs [--latest] [<revision>...]
//
// Without revisions, all results are compared, ordered by the date of the commit they were produced from. Otherwise,
// only the results produced from the given revisions (refs or commit hashes) are compared, in the given order. Results
// produced from a working tree with uncommitted changes ("dirty") count as the revision they're based on, and follow
// its clean results. With --latest, only the most recent result of every (clean or dirty) revision is compared.
//
// Each benchmark score is normalized to the first result that has a score for that benchmark.
import path from 'path';
import { readdirSync, readFileSync, writeFileSync } from 'fs';
import { URL, fileURLToPath } from 'url';
import { parseArgs } from 'util';
import { simpleGit } from 'simple-git';
import { D3Node } from 'd3-node';
import * as d3 from 'd3';

const __dirname = fileURLToPath(new URL('.', import.meta.url));
const repositoryRoot = path.join(__dirname, '..');
const jmhDirectory = path.join(repositoryRoot, 'build/reports/jmh/');
const git = simpleGit(repositoryRoot);

const { values: options, positionals: revisions } = parseArgs({
    options: {
        latest: { type: 'boolean', default: false },
    },
    allowPositionals: true,
});

// See RunJmh for how result files are named
const resultFilePattern = /^(\d{4})(\d{2})(\d{2})-(\d{2})(\d{2})(\d{2})-([0-9a-f]+|unknown)(-dirty)?\.json$/;

async function readCommit(hash) {
    if (hash === 'unknown') {
        return null;
    }
    const output = await git.raw(['show', '-s', '--format=%H%x00%cI%x00%s', hash]).catch(() => null);
    if (!output) {
        // i.e. the commit has been garbage collected after a rebase
        return null;
    }
    const [fullHash, date, subject] = output.trim().split('\0');
    return { hash: fullHash, date: new Date(date), subject };
}

const commits = new Map();
let runs = [];
for (const file of readdirSync(jmhDirectory)) {
    const match = resultFilePattern.exec(file);
    if (!match) {
        continue;
    }
    const [, year, month, day, hours, minutes, seconds, hash, dirty] = match;
    let results;
    try {
        results = JSON.parse(readFileSync(path.join(jmhDirectory, file), 'utf8'));
    } catch (e) {
        console.warn(`Skipping ${file}: ${e.message}`);
        continue;
    }
    if (!commits.has(hash)) {
        commits.set(hash, await readCommit(hash));
    }
    runs.push({
        file,
        timestamp: new Date(year, month - 1, day, hours, minutes, seconds),
        hash,
        dirty: dirty !== undefined,
        revision: hash + (dirty ?? ''),
        commit: commits.get(hash),
        results,
    });
}

// Clean results before dirty ones, and older results before newer ones
const byRevisionThenTime = (a, b) => (a.dirty - b.dirty) || (a.timestamp - b.timestamp);

if (revisions.length === 0) {
    // Results with an unknown commit are ordered by the time they were produced instead
    const sortDate = run => run.commit?.date ?? run.timestamp;
    runs.sort((a, b) => (sortDate(a) - sortDate(b)) || byRevisionThenTime(a, b));
} else {
    const selectedRuns = [];
    for (const revision of revisions) {
        const hash = (await git.revparse(['--verify', '-q', `${revision}^{commit}`]).catch(() => '')).trim();
        if (!hash) {
            console.error(`Unknown revision: ${revision}`);
            process.exit(1);
        }
        const matchingRuns = runs.filter(run => run.commit?.hash === hash).sort(byRevisionThenTime);
        if (matchingRuns.length === 0) {
            console.warn(`No results for revision ${revision}`);
        }
        selectedRuns.push(...matchingRuns);
    }
    runs = selectedRuns;
}

if (options.latest) {
    const latestRuns = new Map();
    for (const run of runs) {
        const latest = latestRuns.get(run.revision);
        if (!latest || run.timestamp > latest.timestamp) {
            latestRuns.set(run.revision, run);
        }
    }
    runs = runs.filter(run => latestRuns.get(run.revision) === run);
}

if (runs.length === 0) {
    console.error(`No benchmark results found in ${jmhDirectory}. Run ./gradlew jmh first.`);
    process.exit(1);
}

function formatTimestamp(date) {
    const pad = n => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

// Only tell results apart by the time they were produced if there are several for the same revision
const revisionCounts = d3.rollup(runs, group => group.length, run => run.revision);
function runLabel(run, maxSubjectLength) {
    let label = run.revision;
    if (revisionCounts.get(run.revision) > 1) {
        label += ` @${formatTimestamp(run.timestamp)}`;
    }
    if (run.commit && maxSubjectLength > 0) {
        const subject = run.commit.subject;
        label += ' ' + (subject.length > maxSubjectLength ? subject.substring(0, maxSubjectLength - 1) + '…' : subject);
    }
    return label;
}

// Shows "Class.method" rather than the fully qualified benchmark name, along with its parameters
function benchmarkKey(result) {
    let key = result.benchmark.split('.').slice(-2).join('.');
    const params = Object.entries(result.params ?? {});
    if (params.length > 0) {
        key += `[${params.map(([name, value]) => `${name}=${value}`).join(',')}]`;
    }
    return key;
}

// Throughput scores grow with better performance, while the others (average time, etc.) shrink
function isHigherBetter(result) {
    return result.mode === 'thrpt';
}

// Collect all benchmarks from all results, since benchmarks may be added or removed along the way
const benchmarks = [...new Set(runs.flatMap(run => run.results.map(benchmarkKey)))].sort();

const data = benchmarks.map((benchmark, seriesIndex) => {
    let baseline;
    const points = runs.map((run, runIndex) => {
        const result = run.results.find(result => benchmarkKey(result) === benchmark);
        if (!result) {
            return { runIndex, missing: true };
        }
        const { score, scoreError, scoreUnit, scoreConfidence: [lower, upper] } = result.primaryMetric;
        // The first result for this benchmark serves as its baseline
        baseline ??= score;
        // Speedup factor, so that higher is better regardless of the benchmark mode
        const factor = s => isHigherBetter(result) ? s / baseline : baseline / s;
        // JMH reports NaN confidence intervals if there is only a single iteration
        const lci = factor(Number.isFinite(lower) ? lower : score);
        const uci = factor(Number.isFinite(upper) ? upper : score);
        return {
            runIndex,
            missing: false,
            score,
            scoreError,
            scoreUnit,
            y: factor(score),
            lci: Math.min(lci, uci),
            uci: Math.max(lci, uci),
        };
    });
    return {
        benchmark,
        color: d3.schemeCategory10[seriesIndex % d3.schemeCategory10.length],
        data: points,
    };
});

printTable();
renderChart();

function printTable() {
    console.log('Results:');
    runs.forEach((run, i) => console.log(`  [${i + 1}] ${runLabel(run, 60)}`));
    console.log();

    const formatNumber = n => Number.isFinite(n) ? n.toPrecision(4) : '?';
    const header = ['Benchmark', ...runs.map((run, i) => `[${i + 1}]`)];
    const rows = data.map(series => [
        series.benchmark,
        ...series.data.map(d => d.missing
            ? '-'
            : `${formatNumber(d.score)} ± ${formatNumber(d.scoreError)} ${d.scoreUnit} (${d.y.toFixed(2)}x)`),
    ]);
    const widths = header.map((_, column) => Math.max(...[header, ...rows].map(row => row[column].length)));
    const formatRow = row => row.map((cell, column) => cell.padEnd(widths[column])).join('  ');
    console.log(formatRow(header));
    rows.forEach(row => console.log(formatRow(row)));
}

function renderChart() {
    const width = 1280
    const height = 720
    const margin = {top: 50, right: 40, bottom: 200, left: 100}
    const chartWidth = width + margin.right + margin.left
    const chartHeight = height + margin.top + margin.bottom
    const errorBarWidth = 10

    const d3n = new D3Node()

    // Results are keyed by their index, since there may be several for the same revision
    const runLabels = runs.map(run => runLabel(run, 40));
    const x = d3.scaleBand().domain(d3.range(runs.length)).range([0, width]).paddingOuter(0.5).paddingInner(1)
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
        .call(d3.axisBottom(x).tickFormat(i => runLabels[i]))
        .selectAll("text")
        .attr("text-anchor", "end")
        .attr("transform", "rotate(-35)")
        .attr("dx", "-0.5em")
        .attr("dy", "0.5em");

    g.append("text")
        .attr("x", 200)
        .attr("y", -10)
        .attr("font-size", 28)
        .text("Speedup factor, normalized to first result, higher is better")

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
        .attr("d", d3.line().defined(d => !d.missing).x(d => x(d.runIndex)).y(d => y(d.y)));
    d.selectAll("path.error-bar")
        .data(series => series.data.filter(d => !d.missing))
        .join("path")
        .classed("error-bar", true)
        .attr("transform", (d) => `translate(${x(d.runIndex)},${y(d.y)})`)
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

    const chartFile = path.join(jmhDirectory, 'chart.svg');
    writeFileSync(chartFile, d3n.svgString())
    console.log();
    console.log(`wrote chart to ${chartFile}`);
}
