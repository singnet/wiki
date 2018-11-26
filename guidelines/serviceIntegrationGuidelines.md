![singnetlogo](assets/singnet-logo.jpg 'SingularityNET')

[singnet-home]: https://www.singularitynet.io
[singnet-github]: https://github.com/singnet
[contribution-guidelines]: https://github.com/singnet/wiki/blob/master/guidelines/CONTRIBUTING.md
[cpp-tutorial]: https://github.com/singnet/wiki/tree/master/tutorials/howToWriteCPPService
[java-tutorial]: https://github.com/singnet/wiki/tree/master/tutorials/howToWriteJavaService
[go-tutorial]: https://github.com/singnet/wiki/tree/master/tutorials/howToWriteGoService
[python-tutorial]: https://github.com/singnet/wiki/tree/master/tutorials/howToWritePythonService
[opencog-tutorial]: https://github.com/singnet/wiki/tree/master/tutorials/howToWriteOpencogService
[service-tutorial]: https://github.com/singnet/wiki/tree/master/tutorials/howToPublishService
[grpc]: https://grpc.io/
[grpc-docs]: https://grpc.io/docs/
[opencog]: https://opencog.org/
[opencog-services]: https://github.com/singnet/opencog-services

# Guidelines for service integration in SingularityNET

Are you ready to contribute to SingularityNET ? We'd love to have you on board,
and we will help you as much as we can. Here are the guidelines we'd like you
to follow so that we can be of more help:

-   [Supported languages](#languages)
-   [AI frameworks](#frameworks)
-   [Contributing to existing projects](#contributing)
-   [Third-party code and models](#thridparty)

## <a name="languages"></a> Supported languages

SingularityNET services use [gRPC][grpc] which is an open-source universal RPC
framework. So any new service must provide its API in gRPC.

gRPC supports several programming languages and a guide for each of them is
available [here][grpc-docs].

- C++
- Java
- Python
- Go
- Ruby
- C#
- Node.js
- Android Java
- Objective-C
- PHP

There are tutorials with step-by-step instructions for implementing a new
service in each of these languages:

- [How to write a SingularityNET service in C++][cpp-tutorial]
- [How to write a SingularityNET service in Python][python-tutorial]
- [How to write a SingularityNET service in Java][java-tutorial]
- [How to write a SingularityNET service in Go][go-tutorial]

If you already have a gRPC service, this tutorial explains how to publish it in
SingularityNET:

- [How to publish a service][service-tutorial]

## <a name="frameworks"></a> AI frameworks

There are a couple of AI frameworks integrated to SingularityNET so you
can just add new functionalities to services which are already published.

- [Opencog][opencog]: an open-source software project aimed at directly
confronting the AGI challenge, using mathematical and biological inspiration
and professional software engineering techniques.

There are tutorials with step-by-step instructions on how to extend the
existing AI framework service in order to implement new functionalities:

- [How to write an Opencog service to SingularityNET][opencog-tutorial]


## <a name="contributing"></a> Contributing to existing projects

SingularityNET have several AI service integration projects. See our
[github][singnet-github] for a list of them.

If you want to contribute to one of our projects please read our
[contribution guidelines][contribution-guidelines].

## <a name="#thridparty"></a> Third-party code and models

Before publishing a service base on third-party code or model(s), it's
important to follow these guidelines:

- 

