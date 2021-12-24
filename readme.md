# Lightweight HTTP Adaptor

## Goal

This is a personal project to attempt to solve a problem I faced several times in my career -- writing custom clients to adapt the request and response objects for different webservices. The inputs and outputs I cared about from a business perspective were almost always the same variables, but they had to be shuffled into different orders and modified with small string substitutions and injected with context-dependent values. All that custom code frequently lead to inconsistent quality controls and operations between the clients.

My goal is to write a **single** client that can work to adapt any (relatively simple) arbitrary request/response model with an intuitive configuration file.

It's also been a long time since I did anything outside of the context of my employer, so I'm hoping to use this as an opportunity to build practice with Maven and Kotlin.

## V1 Deliverables:

Ability to:
- Map arbitrary inputs to the headers and body of POST requests.
- Map the headers and body of POST responses to an arbitrary output.
- Map unexpected POST responses to a standardized set of modeled exceptions.
- Store inputs and the results of previous requests in context, and then use these values to dynamically swap out placeholders.
- Chain several requests together, feeding the responses of one into context to be used by another.
- The usual quality controls -- linting, unit tests, integration tests.

### Example of the finished product

Let's say we run a Bar Service. Customers call our API GetBeer:

```json
api.barservice.dev
POST /beers/
Request:
{
    "customer": string,
    "brewery": string,
    "beer": string
}
Response:
{
    "status": "SUCCESS|FAIL"
}

Examples:
POST /beers/
{
    "customer": "daniel",
    "brewery": "Elysian",
    "beer": "Space Dust"
},
POST /beers/
{
    "customer": "greg",
    "brewery": "AB",
    "beer": "Budweiser|Light"
}
```

We then need to route these requests to particular Brewery services. These services ensure that a person is delivered their beer. Unfortunately,
every brewery has a different API model. For example, AB:

```json
api.ab.dev
POST /bud/
Headers: {
    "x-api-key": Base64 encoded string
}
Body: {
    "person": string,
    "brand": string,
    "subtype": string
}
Response: {
    "status": 201
}

Example:
POST /bud/
Headers: {
    "x-api-key": "abcdef"
}
Body: {
    "person": "greg",
    "brand": "budweiser",
    "subtype": "light"
}
```

Here's a few things to note:
- Auth is provided by a Base64 API key which we know ahead of time, provided as a header.
- Our field "customer" is called "person" by the AB service.
- Our single-string "Budweiser|Light" SKU has been split into two subfields.
- Success is indicated by a 201 code.

On the other hand, here's an example of the Elysian service, which is split between an OAuth endpoint and an API endpoint:

```json
auth.elysian.dev
POST /token/
Headers: {
    "x-oauth-client-secret": string,
    "x-oauth-client-id": string,
}
Body: {
    "person": string
}
Response: {
    "token": string,
    "status": "200",
}

api.elysian.dev
POST /brews/
Headers: {
    "x-token": string
}
Body: {
    "imbiber": string,
    "brew": string
}
Response: {
    "status": "200",
}

Example:
POST /token/
Headers: {
    "x-oauth-client-secret": "abcdef",
    "x-oauth-client-id": "1234",
}
Body: {
    "person": "daniel"
}
Response: {
    "token": "zxcvb",
    "status": "200",
}

api.elysian.dev
POST /brews/
Headers: {
    "x-token": "zxcvb"
}
Body: {
    "imbiber": "daniel",
    "brew": "Space Dust"
}
Response: {
    "status": "200",
}
```

For this integration:
- Auth is provided by an OAuth token, which we fetch in a separate call to an auth service, and then use in a subsequent call.
- Auth for the OAuth call is provided by a ClientId/ClientSecret plumbed through the header.
- Our field "customer" is called "person" by the auth service, but "imbiber" by the beer service.
- Our field "beer" is called "brew".
- Success is indicated by a 200 code.

### The point of the example

Elysian and AB are big companies, and I can't make them conform their APIs to the model
of my choosing. I could write an adapter for each one, but the more beers I serve, the more
adapters I have to write, creating a combinatorial explosion of custom code. However,
none of the APIs are very different -- they largely differ in string mappings, and I contend
I could accomplish any integration with a simple configuration file and parser.
