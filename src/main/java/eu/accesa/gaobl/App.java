package eu.accesa.gaobl;

import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;

import java.io.IOException;

public class App {

    public static int GRPC_PORT = Integer.parseInt(System.getenv("GRPC_PORT"));

    public static void main(String[] args) throws IOException, InterruptedException {
        try (Environment env = Environments.newInstance("tmp/storage")) {
            var app = new GaoBlValidatorApp(env);
            var server = new GrpcServer(app, GRPC_PORT);
            server.start();
            server.blockUntilShutdown();
        }
    }

//    public static void main(String[] args) throws IOException, InterruptedException {
//        try (PersistentEntityStore env = PersistentEntityStores.newInstance("tmp/storage")) {
//            var app = new GaoBlValidatorApp(env);
//            var server = new GrpcServer(app, 26658);
//            server.start();
//            server.blockUntilShutdown();
//        }
//    }
}
