package org.example.basicapp;

import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardEventStream;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardRequest;
import software.amazon.awssdk.services.kinesis.model.SubscribeToShardResponseHandler;

public class SubscribeRecord {

    public static void main(String[] args) {

        Region region = Region.AP_NORTHEAST_1;
        KinesisAsyncClient client = KinesisAsyncClient.builder().region(region).build();

        SubscribeToShardRequest request = SubscribeToShardRequest.builder()
                .consumerARN(SettingInfo.CONSUMER_ARN).shardId(SettingInfo.SHARD_ID)
                .startingPosition(s -> s.type(ShardIteratorType.LATEST)).build();

        SubscribeToShardResponseHandler responseHandler = SubscribeToShardResponseHandler.builder()
                .onError(t -> System.err.println("Error during stream - " + t.getMessage()))
                .subscriber(MySubscriber::new).build();

        client.subscribeToShard(request, responseHandler);
        client.close();
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
