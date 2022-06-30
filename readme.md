# brik

## Scope

* Data modelling
* Database schema generation
* Generate migrations, apply, track them
* CRUD rest endpoints generation
* Admin interface
* Modular project management

## Design principles

* malli is the central way to describe data
* escape hatches everywhere
* everything as code
* developer experience is paramount (and REPL is king)
* solved things should be available as composable modules

## Terms (also in relationship to other projects)

* Modules: They are like mini-apps, vertical slices of the stack (HTTP, model,
  database) that could run (almost) independently. They are called "apps" in
  Django. In Brik, modules are not isolated from each other, the schema of one
  module can refer to the schema of a different module.
* Facets: Each module has different facets and each implements a different part
  of the module's functionality. One facet could provide the HTTP routes for
  this particular module, another facet could provide the malli schema, and yet
  another could provide the database schema. All the facets are optional, so
  it's entirely possible for module to provide a mallin schema facet without
  providing any HTTP routes.
* Cement: The part of Brik code that accepts multiple modules and connects them
  together into a single application. Cement code is also responsible for
  validating that the modules can be combined and reporting any errors
  concerning unmet requirements or clashes between modules.
* Times: Different facets of modules are used at different "times". The HTTP
  route facets are used at "run time" in order to construct the HTTP service,
  malli schemas are used at "migration time" to migrate the database, the CI
  facet is used to generate the CI code during "ops time".

## Examples of escape hatches

What happens when your project grows too complex for Brik? Or when you have some
weird requirement that doesn't fit the framework? You should use one of the
escape hatches that will allow you to write custom code that will co-exist with
the existing Brik code.

Some examples of ways to escape the framework:

* Skip generating specific HTTP routes for a schema and provide your
  implementation instead.
* Replace some of the DB interaction code for with your implementation.
* ???

## Differences to Arachne

* Brik does not use RDF
* Brik does not attempt to abstract the stack, so it does not allow you to
  replace the core technologies: It's built using reitit, malli, PostgreSQL
  (???) and you're stuck with these choices.

## Relationship to integrant

???
