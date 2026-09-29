
# Scripts

format_guidebook.mjs: Reformats the Markdown in the AE2 guidebook

The `jmh` Gradle task writes the results of every run to `build/reports/jmh/<timestamp>-<commit>[-dirty].json`
(and a `.txt` log), tagged with the commit it ran on, and `-dirty` if the working tree had uncommitted changes.

run_jmh_tests.mjs: Runs the JMH benchmarks on several commits. Use `--count <n>` to benchmark the current commit and
the `n - 1` commits before it, or pass a list of refs to benchmark those in the given order. `--iterations <n>` changes
the number of measurement iterations (default: 20).

generate_report.mjs: Compares all results in `build/reports/jmh`, ordered by commit date, prints a table of the scores
and renders them as a chart to `build/reports/jmh/chart.svg`. Pass a list of refs to only compare the results of those
commits, in the given order (the first one is the baseline). `--latest` only uses the most recent result per commit.
