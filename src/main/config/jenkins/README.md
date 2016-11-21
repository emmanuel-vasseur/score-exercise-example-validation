# BattleCode Jenkins Configuration #

Example of configuration for using score-project with your battle-code exercise in a Jenkins server

### Dependencies ###

* Java 8
* Git
* Maven 3.3+
* Jenkins 2.33+
* Jenkins recommended plugins + maven & dsl plugin

## Installation ##

Install Jenkins (via a docker container or any another method).

Install plugins.

Create teams as jenkins users.

Create a 'seed' project that clone your exercise repository, with a DSL step that point on your jobs DSL creation.

Launch your 'seed' project in order to create all jobs.

Enjoy !
