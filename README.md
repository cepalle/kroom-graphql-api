## Sangria akka-http Example

An example [GraphQL](https://graphql.org) server written with [akka-http](https://github.com/akka/akka-http), [circe](https://github.com/circe/circe) and [sangria](https://github.com/sangria-graphql/sangria).

After starting the server with

```bash
sbt run

# or, if you want to watch the source code changes
 
sbt ~reStart
``` 

you can run queries interactively using [graphql-playground](https://github.com/prisma/graphql-playground) by opening [http://localhost:8080](http://localhost:8080) in a browser or query the `/graphql` endpoint directly. The HTTP endpoint follows [GraphQL best practices for handling the HTTP requests](http://graphql.org/learn/serving-over-http/#http-methods-headers-and-body).

# Note

````scala
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
    
case class AuthenticationException(message: String) extends Exception(message)
case class AuthorisationException(message: String) extends Exception(message)

val errorHandler = ExceptionHandler {
  case (m, AuthenticationException(message)) ⇒ HandledException(message)
  case (m, AuthorisationException(message)) ⇒ HandledException(message)
}

Executor.execute(schema, queryAst,
  userContext = new SecureContext(token, userRepo, colorRepo),
  exceptionHandler = errorHandler)


Try[T]



````
