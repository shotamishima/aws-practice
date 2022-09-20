package org.example.basicapp;

/**
 * Hello world!
 *
 */


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardEvent;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardEventStream;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardRequest;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardResponse;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardResponseHandler;


public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        Region region = Region.US_EAST_1;
        KinesisAsyncClient client = KinesisAsyncClient.builder().region(region).build();

        SubscribeToShardRequest request = SubscribeToShardRequest.builder()
                .consumerARN(SettingInfo.CONSUMER_ARN).shardId("arn:aws:kinesis:us-east-1:")
                .startingPosition(s -> s.type(ShardIteratorType.LATEST)).build();

        SubscribeToShardResponseHandler responseHandler = SubscribeToShardResponseHandler.builder()
                .onError(t -> System.err.println("Error during stream - " + t.getMessage()))
                .subscriber(MySubscriber::new).build();
    }

    /**
     * Simple subscriber implementation that prints events and cancels the subscription after 100
     * evnets.
     */
    private static class MySubscriber implements Subscriber<SubscribeToShardEventStream> {

        private Subscription subscription;
        private AtomicInteger eventCount = new AtomicInteger(0);

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            this.subscription.request(1);
        }

        @Override
        public void onNext(SubscribeToShardEventStream shardSubscriptionEventStream) {
            System.out.println("Received event " + shardSubscriptionEventStream);
            if (eventCount.incrementAndGet() >= 100) {
                subscription.cancel();
            }
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("Error occurred while stream - " + throwable.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("Finished streaming all events");
        }
    }
}
