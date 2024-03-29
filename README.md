# ARCHIVED --> see https://github.com/ajoecker/gauge-services

# Gauge Graphql Test Project  
## Idea  
To have a simple and easy-usable project for testing graphql.  

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ajoecker/gauge-graphql/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ajoecker/gauge-graphql) [![Build Status](https://travis-ci.org/ajoecker/gauge-graphql.svg?branch=master)](https://travis-ci.org/ajoecker/gauge-graphql)

## Implementation  
The implementation uses [http://gauge.org](http://gauge.org) as describer and runner of the automated tests and [http://rest-assured.io/](http://rest-assured.io/) as helper library.  
  
## Execution  
### Run all test cases  
To execute all specs call `mvn clean install`. This will build the project and run all test cases  
  
### Run only a specific test case  
To execute a specific test case you can  
* call `mvn clean install -DspecsDir="specs/<spec_to_execute>` to run a complete spec with all test cases  
* call `mvn clean install -DspecsDir="specs/<spec_to_execute> -Dscenario=<name_of_scenario>` to run a single scenario of a spec  
  
## Usage  
To use the library in a project simply put the following in the `pom.xml`  
```  
<dependency>  
 <groupId>com.github.ajoecker</groupId>
 <artifactId>gauge-graphql</artifactId>
 <version>0.2</version>
 <scope>test</scope>
</dependency>  
``` 
As long as the library is not part of a maven central you can either add it directly in our project or install it
in your local maven repository.

## Examples
The project [gauge-graphql-example](https://github.com/ajoecker/gauge-graphql-example) shows some examples of the usage.

## New Testcases  
To add new test cases one can either create a new spec file with the scenario(s) or add the new scenario to an   
existing spec.  
  
### Building blocks  
To add a new test case (scenario) one can re-use existing building blocks for the sake of simplicity. 

#### Define graphql endpoint
The library allows to ways for defining the graphql endpoint to test.

- In the environment of Gauge with the key `graphql.endpoint`
- In a spec file as a common step `* Use "http://the-endpoint`

The first one can be used to define a common endpoint for all specs and can be varied by using multiple gauge environments.

The second can be used to define an endpoint on a spec based level and allows more flexibility if needed.
  
#### Login required  
If a login is required to execute subsequent queries, the first step of a scenario must be
  
`Given "<email>" logs in with password "<password>"`  
  
whereas `<email>` must be an existing customer email and `<password>` must be the matching password of the customer.  See also [Configuration](#Configuration)

If the login is working on a common token and not a dynamic created one, the first step can also be

`Given user logs in`
  
#### Sending graphql  
To send a query/mutation one creates a file in the `src/test/resources` folder and use this file in the sending step  
  
`When sending <file:src/test/resources/the_file_to_send>`  
  
whereas `the_file_to_send` is then the name of file.  

*note* - it is not required to have the querie files inside the `resources` folder. They can reside in any folder relative to the project. The step definition `When sending...` must mirror then the path to the file.
  
#### Verifying the result  
To verify a response multiple building blocks exist. All of them can either start with `Then` or when chaining multiple verifications with `And`.  
  
Also all verification steps have the json path of the attribute to verify as first parameter (see examples below)  
  
##### Must Be  
Verifies that the returned value is a certain value, whereas value can either be a single value or a table.  
  
###### Examples  
`* Then "vehicle.price" must be "720"` 

`* Then "cities.name" must be "New York, London"`  
```  
* Then "brands" must be`   
 |id |name            | 
 |---|----------------|
 |10 |OREO            | 
 |73 |NUTELLA         |
```  
If the given response path returns a list with multiple attributes, one can also state a map like pattern

`* Then "popular_artists.artists" must be "{name: Pablo Picasso, nationality: Spanish}, {name: Banksy, nationality: British}"`

##### Must contain
Verifies that the returned value contains a certain value.

This follows the same blocks as in [Must Be](#must-be)

##### Must Be Empty  
Verifies that the returned value is empty  

###### Examples  
`* Then "cities.city" must be empty`  
  
#### Chaining result verifications  
When multiple values shall be verified, each verification is one step in the scenario. The second and later  
verification can start with `And` instead of `Then` for better reading purpose. 
 
##### Example  
`* Then "vehicle.price" must be "720"`  
`* And "breakfast.brand.name" must be "Nutella"`    
`* And "breakfast.brand.calories" must be "Oh Hell NOOOO"`  

#### Dynamic graphql queries
It is possible to use dynamic graphql queries, when using variables in the graphql file.
##### Example
```
popular_artists(size: $size) {
    artists {
        name
        nationality
    }
}
``` 
When using variables, the `When` step in the spec file must replace this variable to get a valid qraphql.

Like
 
 `* When sending <file:/src/test/resources/popular_artists_variable.graphql> with "size:4"`
 
It is possible to configure the string that masks the variable in the graphql file (default: `$`), via the configuration
`graphql.variable.mask`.

It is also possible to configure the seperator that divides the variable name with the variable value in the step (default `:`), 
via the configuration `graphql.variable.seperator`.

It is also possible to facilitate gauge table for dynamic replacement
```
* When sending <file:/src/test/resources/popular_artists_variable.graphql> with 

   |name|value|
   |----|-----|
   |size|4    |
```
whereas the column headers must be named `name` and `value`.

It is also possible to use the result of a previous request as substitute for a variable
```
## stations around Frankfurt with table
* When sending <file:/src/test/resources/dbahn_frankfurt.graphql>
* And sending <file:/src/test/resources/dbahn_frankfurt_nearby.graphql> with 

   |name     |value                                |
   |---------|-------------------------------------|
   |latitude |$stationWithEvaId.location.latitude  | 
   |longitude|$stationWithEvaId.location.longitude |
   |radius   |2000                                 |
```
the first two values are masked to identify them as variables and contain the full path to a single value (list values are currently not supported).

The values are used in the second request to replace any variables in the query named `latitude` and `longitude`.
## Configuration
In the Gauge environment the following keys are recognized
 
### graphql.endpoint
*Mandatory*

The url to the graphql api. Only mandatory if the endpoint is not given in the spec file directly.
 
### graphql.debug
*Optional*

Will add some request debug information on the console (uses restassured `.log().all()` for this)
 
### graphql.token
*Optional*

In case there is a common token for login instead of a dynamic one (see `graphql.token.query`)
 
### grapqh.token.query
*Optional*

Name of the file, containing the query for the login. This file must be located in the `src/test/resources` folder and the username/email and password must be masked with `%s`.
#### Example
```
mutation {  
    login(email: "$user", password: "$password") {  
        token  
    }  
}
```
### graph.token.path
*Optional*

When a *graph.token.query* is given, this becomes *mandatory* as it gives the jsonpath from which the token can be extracted from the response of the login query. 

E.g. in the above query example, the `graph.token.path` could be `data.login.token`
 
### graph.seperator
*Optional*

Defines the seperator in the verifying step to define multiple elements that needs to be verified. Default is `,`
#### Example
`* Then "popular_artists.artists.name" must contain "Pablo Picasso, Banksy"`

### graph.variable.mask
*Optional*

Defines the string that masks a variable in the graphql file
#### Example
```
popular_artists(size: $size) {
    artists {
        name
        nationality
    }
}
``` 
### graph.variable.seperator
*Optional*

Defines the seperator of variable name and variable value in the step. Default is `:`
#### Example
 `* When sending <file:/src/test/resources/popular_artists_variable.graphql> with "size:4"`

## Note  
Gauge does not support currently multiline parameters, which means a query cannot be part of the step, but must  be referenced by an external file. Watch https://github.com/getgauge/gauge/issues/175 for this.
