# Micronaut GraphQL Tools

[![Maven Central](https://img.shields.io/maven-central/v/io.micronaut.xxx/micronaut-xxx.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.micronaut.xxx%22%20AND%20a:%22micronaut-xxx%22)
[![Build Status](https://github.com/expatiat/micronaut-graphql-tools/workflows/Java%20CI/badge.svg)](https://github.com/expatiat/micronaut-graphql-tools/actions)

Micronaut GraphQL Tools is...

## Documentation

See the [Documentation](https://expatiat.github.io/micronaut-graphql-tools/latest/guide/) for more information.

See the [Snapshot Documentation](https://expatiat.github.io/micronaut-graphql-tools/snapshot/guide/) for the current development docs.

## Examples

Examples can be found in the [examples](https://github.com/expatiat/micronaut-graphql-tools/tree/master/examples) directory.

## Snapshots and Releases

Snaphots are automatically published to [Sonatype Snapshots](https://oss.sonatype.org/content/repositories/snapshots/) using [Github Actions](https://github.com/expatiat/micronaut-graphql-tools/actions).

See the documentation in the [Micronaut Docs](https://docs.micronaut.io/latest/guide/index.html#usingsnapshots) for how to configure your build to use snapshots.

Releases are published to Maven Central via [Github Actions](https://github.com/expatiat/micronaut-graphql-tools/actions).

Releases are completely automated. To perform a release use the following steps:

* [Publish the draft release](https://github.com/expatiat/micronaut-graphql-tools/releases). There should be already a draft release created, edit and publish it. The Git Tag should start with `v`. For example `v1.0.0`.
* [Monitor the Workflow](https://github.com/expatiat/micronaut-graphql-tools/actions?query=workflow%3ARelease) to check it passed successfully.
* If everything went fine, [publish to Maven Central](https://github.com/expatiat/micronaut-graphql-tools/actions?query=workflow%3A"Maven+Central+Sync").
* Celebrate!
