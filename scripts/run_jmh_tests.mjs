import { simpleGit } from 'simple-git';
import { spawnSync } from 'child_process';
import path from 'path';
import { readFileSync, writeFileSync } from 'fs';
import { URL, fileURLToPath } from 'url';

const __dirname = fileURLToPath(new URL('.', import.meta.url));
const repositoryRoot = path.join(__dirname, '..');
let jmhDirectory = path.join(repositoryRoot, 'build/results/jmh/');
const git = simpleGit()

// do one warmup run before the main sequence
console.log("executing warmup run to warm fs cache")
spawnSync(path.join(repositoryRoot, 'gradlew'), ['jmh', '-Pjmh.iterations=3'], { 'cwd': repositoryRoot })
const startCommit = await git.revparse(['--abbrev-ref', 'HEAD'])
try {
    const commits = await git.log({ 'from': startCommit, 'to': 'forge/1.20.1' })

    var allResults = []
    for (const commit of commits.all) {
        console.log('testing commit ' + commit.hash )
        await git.checkout(commit.hash)
        const gradleCommand = spawnSync(path.join(repositoryRoot, 'gradlew'), ['jmh', '-Pjmh.iterations=20'], { 'cwd': repositoryRoot })
        if (gradleCommand.error) {
            console.log(gradleCommand.error)
            break;
        }
        console.log(gradleCommand.stdout.toString('utf8'))
        console.log(gradleCommand.stderr.toString('utf8'))
        const resultFile = readFileSync(path.join(jmhDirectory, 'results.json'), 'utf8')
        const rawResults = JSON.parse(resultFile)
        for (const benchmarkRun of rawResults) {
            benchmarkRun.hash = commit.hash;
            benchmarkRun.date = commit.date;
            benchmarkRun.message = commit.message;
            benchmarkRun.refs = commit.refs;
            benchmarkRun.body = commit.body;
            benchmarkRun.author_name = commit.author_name;
            benchmarkRun.author_email = commit.author_email;
        }
        allResults.push(rawResults)
        writeFileSync(path.join(jmhDirectory, 'all-results.json'), JSON.stringify(allResults, null, 2))
    }
} finally {
    await git.checkout(startCommit)
}
