# How to contribute

We want to keep it as easy as possible to contribute changes to support
the growth and stability of AE2. There are a few guidelines that we
need contributors to follow so that we can have a chance of keeping on
top of things.

## Getting Started

### Reporting an Issue

Applied Energistics 2 crashing, have a suggestion, found a bug?  Create an issue now!

1. Make sure your issue has not already been answered or fixed and you are using the latest version. Also think about whether your issue is a valid one before submitting it.
    * If it is already possible with vanilla and AE2 itself, the suggestion will be considered invalid.
    * Asking for a smaller version, more compact version, or more efficient version of something will also be considered invalid.
2. Go to [the issues page](https://github.com/AppliedEnergistics/Applied-Energistics-2/issues) and click [new issue](https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/new)
3. If applicable, use on of the provided templates. It will also contain further details about required or useful information to add.
4. Click `Submit New Issue`, and wait for feedback!

Providing as many details as possible does help us to find and resolve the issue faster and also you getting a fixed version as fast as possible.

Please note that we might close any issue not matching these requirements. 

### Submitting Changes

* Submit an issue to the github project, assuming one does not already exist.
  * Clearly describe the issue including steps to reproduce when it is a bug.
  * Make sure you fill in the earliest version that you know has the issue.
  * Waiting for feedback is suggested.
* Fork the repository on GitHub
* Create a topic branch from where you want to base your work.
  * This `master` branch that is under active development.
  * Only target release branches if you are certain your fix must be on that
    branch.
  * To quickly create a topic branch based on the development branch; `git 
    checkout -b my_contribution_branch`. Please avoid working 
    directly on the `master` branch.
* Make commits of logical units.
* Check for unnecessary whitespace with `git diff --check` before committing.
* Make sure your commit messages are in the proper format.
  * You can either use the gradle `spotlessCheck` and `spotlessApply` tasks or use the provided eclipse formatter config in `/codeformat`

````
    (#12345) Make the example in CONTRIBUTING imperative and concrete

    Without this patch applied the example commit message in the CONTRIBUTING
    document is not a concrete example.  This is a problem because the
    contributor is left to imagine what the commit message should look like
    based on a description rather than an example.  This patch fixes the
    problem by making the example concrete and imperative.

    The first line is a real life imperative statement with a ticket number
    from our issue tracker.  The body describes the behavior without the patch,
    why this is a problem, and how the patch fixes the problem when applied.
````
* Always fully test your changes. If they are large enough in scope, then fully test AE2.
* Describing the process you used to test your changes in detail will help speed up this process.

## Making Trivial Changes

### Documentation

For changes of a trivial nature to comments and documentation, it is not always necessary to create a new issue. 
We usually use `squash and merge`. This allows us to easily append the PR number for future reference.

````
    Add documentation commit example to CONTRIBUTING

    There is no example for contributing a documentation commit
    to the Puppet repository. This is a problem because the contributor
    is left to assume how a commit of this nature may appear.

    The first line is a real life imperative statement with '(doc)' in
    place of what would have been the ticket number in a
    non-documentation related commit. The body describes the nature of
    the new documentation or comments added.
````

### Semantic Changes

In order to keep the code in a state where PRs can be safely merged, it is important to
avoid changes to syntax or changes that don't add any real value to the code base. PRs
that make changes only to syntax or "clean up" the code will be rejected. Any code clean-up
should be coordinated with the core team first.


## Style Guidelines

Applied Energistics does not follow standard Java syntax. The guidelines below illustrate
the styling guidelines used by AE. 

PRs that do not conform to these standards will be rejected.

You can find presets for Eclipse in the `codeformat` folder. For IntelliJ IDEA we recommend the eclipse formatter plugin.

There is also a gradle task available to check for syntax errors (`spotlessCheck`) as well as applying the fixes (`spotlessApply`).

We try to keep them up to date. Please report any problem with maintaining the correct formatting.

# Additional Resources

* [General GitHub documentation](http://help.github.com/)
* [GitHub pull request documentation](http://help.github.com/send-pull-requests/)
* #AppliedEnergistics IRC channel on esper.net
