# hivemq-examples
This is a collection of code examples for working with the 
[HiveMQ Extension System](https://www.hivemq.com/docs/4.2/extensions/introduction.html)
and the [HiveMQ MQTT Client](https://github.com/hivemq/hivemq-mqtt-client).

## Enhanced Auth
* math-challenge-extension: An example HiveMQ Extension to demonstrate challenge response authentication.
* math-challenge-client: An example MQTT client to demonstrate  challenge response authentication.

## How to test MQTT Applications
* An example how to test a full MQTT implementation using the [official HiveMQ Testcontainer](https://github.com/hivemq/hivemq-testcontainer).

## Getting started with HiveMQ Cloud
[Sign up for your free HiveMQ Cloud account](https://console.hivemq.cloud/?utm_source=github&utm_medium=repositories&utm_campaign=hivemq-examples) to try out the cloud hosted version of HiveMQ. 
You will get a free HiveMQ Cloud broker instance that you can use in combination with the *hivemq-getting-started-cloud* module of this example.
Enter your host, username and password like it is described in the comments or on the [website](https://www.hivemq.com/blog/how-to-get-started-with-mqtt/).
Then you can see, how an example-implementation would work.

## Getting started with a public HiveMQ broker
To try out the publicly hosted version of HiveMQ, simply use the *hivemq-getting-started-public* module.
It will connect to `"broker.hivemq.com"`, which is the publicly available broker of HiveMQ.
Run the program, and you can see how an example-implementation would work.
