package ru.jokerconf.grpcexample;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherService extends WeatherServiceGrpc.WeatherServiceImplBase {
    ManagedChannel grpcChannel = NettyChannelBuilder.forAddress("localhost", 8091).build();
    private final TemperatureServiceGrpc.TemperatureServiceFutureStub tempService = TemperatureServiceGrpc.newFutureStub(grpcChannel);
    private final HumidityServiceGrpc.HumidityServiceFutureStub humidityService = HumidityServiceGrpc.newFutureStub(grpcChannel);
    private final WindServiceGrpc.WindServiceFutureStub windService = WindServiceGrpc.newFutureStub(grpcChannel);

    @Override
    public void getCurrent(WeatherRequest request, StreamObserver<WeatherResponse> responseObserver) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Coordinates coordinates = request.getCoordinates();
        ListenableFuture<List<WeatherResponse>> responseFuture = Futures.allAsList(
                Futures.transform(tempService.getCurrent(coordinates),
                        (Temperature temp) -> WeatherResponse.newBuilder().setTemperature(temp).build(), executorService),
                Futures.transform(humidityService.getCurrent(coordinates),
                        (Humidity humidity) -> WeatherResponse.newBuilder().setHumidity(humidity).build(), executorService),
                Futures.transform(windService.getCurrent(coordinates),
                        (Wind wind) -> WeatherResponse.newBuilder().setWind(wind).build(), executorService)
        );

        Futures.addCallback(responseFuture, new FutureCallback<>() {
            @Override
            public void onSuccess(@NullableDecl List<WeatherResponse> result) {
                WeatherResponse.Builder response = WeatherResponse.newBuilder();
                result.forEach(response::mergeFrom);
                responseObserver.onNext(response.build());
                responseObserver.onCompleted();
            }

            @Override
            public void onFailure(Throwable t) {
                responseObserver.onError(t);
            }
        }, executorService);

//        //собираем ответ
//        WeatherResponse response = WeatherResponse.newBuilder()
//                .setTemperature(Temperature.newBuilder().setUnits(Temperature.Units.CELSIUS).setDegrees(20.f))
//                .setHumidity(Humidity.newBuilder().setValue(.65f))
//                .build();
//
//        //вернули респонс
//        responseObserver.onNext(response);
//        //завершаем поток
//        responseObserver.onCompleted();
    }
}
