package cn.edu.ustc.irpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class IPUServer {
    private static final Logger logger = Logger.getLogger(IPUServer.class.getName());
    private final int port;
    private final Server server;

    public IPUServer(int port) {
        this.port = port;
        server = ServerBuilder
                .forPort(port)
                .addService(new InferenceService())
                .build();
    }

    //start service
    public void start() throws IOException {
        server.start();
        logger.info("Server started,listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    System.err.println("*** shutting down gRPC server since JVM is shutting down");
                    IPUServer.this.stop();
                    System.err.println("*** server shut down");

                }
        ));
    }

    //stop service
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    // await termination on the main thread since the gRPC library uses daemon threads.
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        IPUServer ipuServer = new IPUServer(8090);
        ipuServer.start();
        ipuServer.blockUntilShutdown();
    }

    private static class InferenceService extends IRPCGrpc.IRPCImplBase {

        private AtomicInteger count = new AtomicInteger(-1);

        @Override
        public void inferenceProcess(InferenceData request, StreamObserver<InferenceResult> responseObserver) {
            List<String> cmds = new ArrayList<>();
//            cmds.add("cd /root/chuangzhi_1A_V4/caffe/examples/cpp_classification");
//            cmds.add("./run-online.sh alexnet 0");
            cmds.add("cd /sdspace");
            cmds.add("chmod +x run.sh");
            cmds.add("./run.sh one two three");
            cmds.add("exit");
            String result = doCMDs(cmds);

            System.out.println("******************************************");
            System.out.println("received the task_" + count.incrementAndGet() + " ...");
            InferenceResult reply = InferenceResult
                    .newBuilder()
                    .setUserID(request.getUserID())
                    .setJobID(request.getJobID())
                    .setResult(result)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
            System.out.println("inference ended...");
        }

        private String doCMDs(List<String> cmds) {
            StringBuilder result = new StringBuilder();
            Runtime run = Runtime.getRuntime();
            File wd = new File("/bin");
            Process proc = null;
            BufferedReader in = null;
            PrintWriter out = null;

            try {
                proc = run.exec("/bin/bash", null, wd);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (proc != null) {
                in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
                for (String cmd : cmds) {
                    out.println(cmd);
                }
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line + "\n");
                    }
                    proc.waitFor();
                    if (proc.exitValue() == 0) {
                        logger.info("Execute the shell successfully!");
                    } else {
                        logger.warning("Fail to execute the shell! ");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (out != null) {
                        out.close();
                    }
                    if (proc != null) {
                        proc.destroy();
                    }
                }
            }
            return result.toString();
        }
    }
}
