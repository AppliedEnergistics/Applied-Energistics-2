// Runs the JMH benchmarks on several commits. The jmh task writes the results of each run to build/reports/jmh/,
// tagged with the commit they were produced from. Use generate_report.mjs afterwards to compare them.
//
// Usage:
//   node run_jmh_tests.mjs --count <n>   Benchmark the current commit and the <n> - 1 commits before it
//   node run_jmh_tests.mjs <ref>...      Benchmark the given refs (branches, tags, commits), in the given order
//
// At the end, this prints the generate_report.mjs command line that compares the benchmarked commits, using the first
// one (the oldest commit for --count, or the first ref) as the baseline.
import { simpleGit } from 'simple-git';
import { spawnSync } from 'child_process';
import path from 'path';
import { mkdirSync, readdirSync, rmSync } from 'fs';
import { URL, fileURLToPath } from 'url';
import { parseArgs } from 'util';

const __dirname = fileURLToPath(new URL('.', import.meta.url));
const repositoryRoot = path.join(__dirname, '..');
const jmhDirectory = path.join(repositoryRoot, 'build/reports/jmh/');
const git = simpleGit(repositoryRoot);

const usage = 'Usage: node run_jmh_tests.mjs --count <n> | <ref>...';
const { values: options, positionals: refs } = parseArgs({
    options: {
        count: { type: 'string', short: 'n' },
        iterations: { type: 'string', short: 'i', default: '20' },
    },
    allowPositionals: true,
});
if ((options.count === undefined) === (refs.length === 0)) {
    console.error(usage);
    console.error('Specify either --count or a list of refs.');
    process.exit(1);
}

// Resolve what to benchmark up front, so that checking out commits cannot change what refs point to
const commits = [];
if (options.count !== undefined) {
    const count = Number.parseInt(options.count, 10);
    if (!(count > 0)) {
        console.error('--count must be a positive number');
        process.exit(1);
    }
    const log = await git.log(['--first-parent', '-n', String(count), 'HEAD']);
    // git log lists the newest commit first, but the baseline should come first
    commits.push(...[...log.all].reverse());
} else {
    for (const ref of refs) {
        const log = await git.log(['-1', ref]).catch(() => null);
        if (!log?.latest) {
            console.error(`Unknown ref: ${ref}`);
            process.exit(1);
        }
        commits.push({ ...log.latest, ref });
    }
}

// Checking out other commits would fail or carry over local changes otherwise
const status = await git.status();
if (!status.isClean()) {
    console.error('The working tree has uncommitted changes. Commit or stash them first.');
    process.exit(1);
}

// Return to the branch we started on, or to the commit if HEAD is detached
const startBranch = (await git.raw(['symbolic-ref', '--short', '-q', 'HEAD']).catch(() => '')).trim();
const startPoint = startBranch || (await git.revparse(['HEAD'])).trim();

// -q skips git's scan for orphaned commits when leaving a detached HEAD, which would abort the checkout
// halfway if the repository contains any broken refs
async function checkout(target) {
    await git.checkout(['-q', target]);
}

function runJmh(iterations) {
    const gradleArgs = ['jmh', `-Pjmh.iterations=${iterations}`];
    const [command, args] = process.platform === 'win32'
        // .bat files can only be run through cmd.exe
        ? [process.env.ComSpec ?? 'cmd.exe', ['/d', '/c', path.join(repositoryRoot, 'gradlew.bat'), ...gradleArgs]]
        : [path.join(repositoryRoot, 'gradlew'), gradleArgs];
    const result = spawnSync(command, args, { cwd: repositoryRoot, stdio: 'inherit' });
    if (result.error) {
        throw result.error;
    }
    return result.status === 0;
}

mkdirSync(jmhDirectory, { recursive: true });

const benchmarkedCommits = [];
const failedCommits = [];
try {
    let warmedUp = false;
    for (const commit of commits) {
        const label = commit.ref ? `${commit.ref} (${commit.hash})` : commit.hash;
        console.log(`testing commit ${label}: ${commit.message}`);
        await checkout(commit.hash);

        if (!warmedUp) {
            // do one warmup run before the main sequence to warm the fs cache
            console.log('executing warmup run to warm fs cache');
            // Its results aren't meant to be compared, so remove them afterwards
            const existingFiles = new Set(readdirSync(jmhDirectory));
            runJmh(3);
            for (const file of readdirSync(jmhDirectory)) {
                if (!existingFiles.has(file)) {
                    rmSync(path.join(jmhDirectory, file), { force: true });
                }
            }
            warmedUp = true;
        }

        if (!runJmh(options.iterations)) {
            console.error(`JMH failed for commit ${label}, skipping it`);
            failedCommits.push(label);
            continue;
        }
        benchmarkedCommits.push(commit.hash.substring(0, 9));
    }
} finally {
    try {
        await checkout(startPoint);
    } catch (e) {
        console.error(`Failed to return to ${startPoint}: ${e.message}`);
        process.exitCode = 1;
    }
}

console.log(`wrote results for ${benchmarkedCommits.length} commit(s) to ${jmhDirectory}`);
if (benchmarkedCommits.length > 0) {
    console.log(`compare them with: node generate_report.mjs ${benchmarkedCommits.join(' ')}`);
}
if (failedCommits.length > 0) {
    console.error(`JMH failed for: ${failedCommits.join(', ')}`);
    process.exitCode = 1;
}
