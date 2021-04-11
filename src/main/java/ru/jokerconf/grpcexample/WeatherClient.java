package ru.jokerconf.grpcexample;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherClient {
    private static Logger logger = LoggerFactory.getLogger(WeatherClient.class);

    public static void main(String[] args) {
        //логическая абстракция соединения между клиентом и сервисом
        ManagedChannel grpcChannel = NettyChannelBuilder.forAddress("localhost", 8090).build();

        /** 3 вида клиентов: */
        //Асинхронный клиент
        WeatherServiceGrpc.WeatherServiceStub client = WeatherServiceGrpc.newStub(grpcChannel);

        //Блокирующий клиент
        WeatherServiceGrpc.WeatherServiceBlockingStub blockingClient = WeatherServiceGrpc.newBlockingStub(grpcChannel);

        //Тоже блокирующий, но на Future
        WeatherServiceGrpc.WeatherServiceFutureStub futureClient = WeatherServiceGrpc.newFutureStub(grpcChannel);

        //самый простой в использовании - блокирующий клиент
        synchronousClientUsage(blockingClient);
        //испльзование асинхронного клиента
        asynchroniousClientUsage(client);
    }

    private static void asynchroniousClientUsage(WeatherServiceGrpc.WeatherServiceStub client) {
        WeatherRequest request = WeatherRequest.newBuilder()
                .setCoordinates(Coordinates.newBuilder().setLatitude(504000000)
                        .setLongitude(-350000000)).build();

        client.getCurrent(request, new StreamObserver<WeatherResponse>() {
            @Override
            public void onNext(WeatherResponse weatherResponse) {
                logger.info("Current weather for {}: {}", request, weatherResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Cannot get weather for {}", request);
            }

            @Override
            public void onCompleted() {
                logger.info("Stream completed.");
            }
        });
    }

    private static void synchronousClientUsage(WeatherServiceGrpc.WeatherServiceBlockingStub blockingClient) {
        WeatherRequest request = WeatherRequest.newBuilder()
                .setCoordinates(Coordinates.newBuilder().setLatitude(420000000)
                        .setLongitude(-720000000)).build();

        WeatherResponse response = blockingClient.getCurrent(request);
        logger.info("Current weather for {}: {}", request, response);
    }
}
