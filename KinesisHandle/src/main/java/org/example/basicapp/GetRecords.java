package org.example.basicapp;

import java.util.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.KinesisException;
import software.amazon.kinesis.exceptions.KinesisClientLibDependencyException;

public class GetRecords {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Need to set Kinesis data stream to where records are got ");
            System.exit(1);
        }

        Region region = Region.AP_NORTHEAST_1;
        String streamName = args[0];
        String shardId = SettingInfo.SHARD_ID;
        KinesisClient kinesisClient = KinesisClient.builder().region(region).build();

        getData(kinesisClient, streamName, shardId);
        kinesisClient.close();
    }

    private static void getData(KinesisClient kinesisClient, String streamName, String shardId) {

        GetShardIteratorRequest getShardIteratorRequest = GetShardIteratorRequest.builder()
                .streamName(streamName).shardId(shardId).shardIteratorType("TRIM_HORIZON").build();
        GetShardIteratorResponse getShardIteratorResponse =
                kinesisClient.getShardIterator(getShardIteratorRequest);
        String shardIterator = getShardIteratorResponse.shardIterator();
        System.out.println("Shard Iterator: " + shardIterator);

        GetRecordsRequest getRecordsRequest =
                GetRecordsRequest.builder().shardIterator(shardIterator).build();
        GetRecordsResponse getRecordsResponse = kinesisClient.getRecords(getRecordsRequest);
        List<Record> records = getRecordsResponse.records();
        // System.out.println(getRecordsResponse.toString());

        System.out.println("Getting records... ");
        for (Record record : records) {
            System.out.println(record.toString());
        }
    }

}
