package ru.jokerconf.grpcexample;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.io.IOException;

public class WeatherServer {

  public static void main(String[] args) throws IOException {
        //Сервер gRPC
        Server grpcServer = NettyServerBuilder.forPort(8090)
                .addService(new WeatherService()).build()
                .start();
    }
}
