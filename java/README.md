# Eclipse Ditto Java client SDK

## Purpose

Deliver a client SDK for Java in order to interact with digital twins provided by an Eclipse Ditto backend.

## Features

* Digital twin management: CRUD (create, read, update, delete) of Ditto [things](https://www.eclipse.dev/ditto/basic-thing.html)
* [Change notifications](https://www.eclipse.dev/ditto/basic-changenotifications.html): 
  consume notifications whenever a "watched" digital twin is modified 
* Send/receive [messages](https://www.eclipse.dev/ditto/basic-messages.html) to/from devices connected via a digital twin
* Use the [live channel](https://www.eclipse.dev/ditto/protocol-twinlive.html#live) in order to react on commands directed
  to devices targeting their "live" state

## Communication channel

The Ditto Java client interacts with an Eclipse Ditto backend via Ditto's 
[WebSocket](https://www.eclipse.dev/ditto/httpapi-protocol-bindings-websocket.html) sending and receiving messages
in [Ditto Protocol](https://www.eclipse.dev/ditto/protocol-overview.html).

## Usage

Maven coordinates:

```xml
<dependency>
   <groupId>org.eclipse.ditto</groupId>
   <artifactId>ditto-client</artifactId>
   <version>${ditto-client.version}</version>
</dependency>
```

### Instantiate & configure a new Ditto client

To configure your Ditto client instance, use the `org.eclipse.ditto.client.configuration` package in order to 
* create instances of `AuthenticationProvider` and `MessagingProvider`
* create a `DisconnectedDittoClient` instance
* obtain a `DittoClient` instance asynchronously by calling `.connect()`

For example:

with Basic authentication:

```java
AuthenticationProvider authenticationProvider =
    AuthenticationProviders.basic(BasicAuthenticationConfiguration.newBuilder()
        .username("ditto")
        .password("ditto")
        .build());
```

or JWT authentication:

```java
// optionally define a proxy server to use
ProxyConfiguration proxyConfig = ProxyConfiguration.newBuilder()
    .proxyHost("localhost")
    .proxyPort(3128)
    .build();

AuthenticationProvider authenticationProvider =
    AuthenticationProviders.clientCredentials(ClientCredentialsAuthenticationConfiguration.newBuilder()
        .clientId("my-oauth-client-id")
        .clientSecret("my-oauth-client-secret")
        .scopes("offline_access email")
        .tokenEndpoint("https://my-oauth-provider/oauth/token")
        .proxyConfiguration(proxyConfig) // optionally configure a proxy server
        .build());
```

```java
MessagingProvider messagingProvider = MessagingProviders.webSocket(WebSocketMessagingConfiguration.newBuilder()
    .endpoint("wss://ditto.eclipse.org")
    .jsonSchemaVersion(JsonSchemaVersion.V_2)
    .proxyConfiguration(proxyConfig) // optionally configure a proxy server
    // optionally configure a truststore containing the trusted CAs for SSL connection establishment
    .trustStoreConfiguration(TrustStoreConfiguration.newBuilder()
        .location(TRUSTSTORE_LOCATION)
        .password(TRUSTSTORE_PASSWORD)
        .build())
    .build(), authenticationProvider);

DisconnectedDittoClient disconnectedDittoClient = DittoClients.newDisconnectedInstance(messagingProvider);

disconnectedDittoClient.connect()
    .thenAccept(this::startUsingDittoClient)
    .exceptionally(error -> disconnectedDittoClient.destroy());
```

### Use the Ditto client

#### Manage twins

```java
client.twin().create("org.eclipse.ditto:new-thing").handle((createdThing, throwable) -> {
    if (createdThing != null) {
        System.out.println("Created new thing: " + createdThing);
    } else {
        System.out.println("Thing could not be created due to: " + throwable.getMessage());
    }
    return client.twin().forId(thingId).putAttribute("first-updated-at", OffsetDateTime.now().toString());
}).get(); // this will block the thread! work asynchronously whenever possible!
```

#### Manage policies

```java
        client.policies().create(newPolicy)
                .thenAccept(createdPolicy -> System.out.println("Created new Policy: " + createdPolicy)).get();

        client.twin()
                .forId(ThingId.of("org.eclipse.ditto:new-thing"))
                .setPolicyId(newPolicy.getEntityId().get())
                .thenAccept(_void -> System.out.println("PolicyId was adapted"))
                .get();
```

#### Subscribe for change notifications

```java
client.twin().startConsumption().get();
System.out.println("Subscribed for Twin events");
client.twin().registerForThingChanges("my-changes", change -> {
   if (change.getAction() == ChangeAction.CREATED) {
       System.out.println("An existing Thing was modified: " + change.getThing());
       // perform custom actions ..
   }
});
```

#### Send/receive messages

Register for receiving messages with the subject `hello.world` on any thing:

```java
client.live().registerForMessage("globalMessageHandler", "hello.world", message -> {
   System.out.println("Received Message with subject " +  message.getSubject());
   message.reply()
      .statusCode(HttpStatusCode.IM_A_TEAPOT)
      .payload("Hello, I'm just a Teapot!")
      .send();
});
```

Send a message with the subject `hello.world` to the thing with ID `org.eclipse.ditto:new-thing`:

```java
client.live().forId("org.eclipse.ditto:new-thing")
   .message()
   .from()
   .subject("hello.world")
   .payload("I am a Teapot")
   .send(String.class, (response, throwable) ->
      System.out.println("Got response: " + response.getPayload().orElse(null))
   );
```

## Further Examples

For further examples on how to use the Ditto client, please have a look at the class 
[DittoClientUsageExamples](src/test/java/org/eclipse/ditto/client/DittoClientUsageExamples.java) which is
configured to connect to the [Ditto sandbox](https://ditto.eclipse.org).
