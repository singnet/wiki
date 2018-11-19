import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaClient {
    private static final Logger logger = Logger.getLogger(JavaClient.class.getName());

    private final ManagedChannel channel;
    private final ServiceDefinitionGrpc.ServiceDefinitionBlockingStub blockingStub;

    public JavaClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().build());
    }

    JavaClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = ServiceDefinitionGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void div(int a, int b) {
        logger.info("Trying to divide "+a+" by "+ b);
        IntPair request = IntPair.newBuilder().setA(a).setB(b).build();
        SingleInt response;
        try {
            response = blockingStub.div(request);
            logger.log(Level.INFO, "Result: " + response.getV());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }
    // TEST_CODE
    public void check(int single) {
        return;
    }

    public static void main(String[] args) throws Exception {
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
        JavaClient client = new JavaClient("localhost", port);
        try {
            System.out.println("Client connected on port: "+ String.valueOf(port));
            client.div(10, 5);
        } finally {
            client.shutdown();
        }
    }
}
