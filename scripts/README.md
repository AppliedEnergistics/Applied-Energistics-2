
# Scripts

format_guidebook.mjs: Reformats the Markdown in the AE2 guidebook

run_jmh_tests.mjs: Runs the JMH benchmarks on several commits and collects the results in `build/reports/jmh/all-results.json`.
Use `--count <n>` to benchmark the current commit and the `n - 1` commits before it, or pass a list of refs
to benchmark those in the given order. `--iterations <n>` changes the number of measurement iterations (default: 20).

generate_report.mjs: Renders the results collected by `run_jmh_tests.mjs` as a chart to `build/reports/jmh/chart.svg`.
