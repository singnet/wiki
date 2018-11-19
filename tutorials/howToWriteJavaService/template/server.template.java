import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Logger;

public class JavaServer {

    private static final Logger logger = Logger.getLogger(JavaServer.class.getName());

    private Server server;

    private void start(int portParam) throws IOException {
        /* The port on which the server should run */
        int port = __SERVICE_PORT__;
        if (portParam > 0){
            port = portParam;
        }
        server = ServerBuilder.forPort(port)
                .addService(new ServiceDefinitionImpl())
                .build()
                .start();
        logger.info("Server listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                JavaServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = __SERVICE_PORT__;
        for (String arg : args) {
             arg = arg.toUpperCase().trim();
            if (arg.contains("--PORT")) {
                String[] argPort = arg.split("=");
                if(Integer.valueOf(argPort[1]).intValue() > 0) {
                    port = Integer.valueOf(argPort[1]).intValue();
                }
             }
        }
        final JavaServer server = new JavaServer();
        server.start(port);
        server.blockUntilShutdown();
    }

    //SERVICE_API
    static class ServiceDefinitionImpl extends ServiceDefinitionGrpc.ServiceDefinitionImplBase {

        @Override
        public void div(IntPair request, StreamObserver<SingleInt> responseObserver) {
            int result = request.getA() / request.getB();
            SingleInt reply = SingleInt.newBuilder().setV(result).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void check(SingleInt request,StreamObserver<SingleString> responseObserver) {
            String result = String.valueOf(request.getV());
            SingleString reply = SingleString.newBuilder().setS(result).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
