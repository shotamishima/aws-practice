package org.example.lambdabasic;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.KinesisException;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;

public class KinesisHandler implements RequestHandler<KinesisEvent, String> {

    @Override
    public String handleRequest(KinesisEvent kinesisEvent, Context context) {

        String response = "200 OK";
        String table_name = "Ticker";
        Region region = Region.AP_NORTHEAST_1;
        String streamName = "terraform-kinesis-out";

        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.standard().build();
        ObjectMapper JSON = new ObjectMapper();
        KinesisClient kinesisClient = KinesisClient.builder().region(region).build();

        for (KinesisEventRecord record : kinesisEvent.getRecords()) {
            try {
                // Deserialize record got from Kinesis
                StockTrade trade =
                        JSON.readValue(record.getKinesis().getData().array(), StockTrade.class);
                System.out.println(trade.toString());

                // transform
                System.out.println(trade.updatePrice(1000));

                // DynamoDB
                Write2Dynamo(ddb, table_name, trade);

                // Put record to another kinesis stream
                PutRecord2OutputKinesis(kinesisClient, streamName, trade);

            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
        }

        return response;
    }

    public static void Write2Dynamo(AmazonDynamoDB ddb, String table_name, StockTrade trade) {
        // Field is applicable only string.
        // Want to change field type for each value's type
        HashMap<String, AttributeValue> item_values = new HashMap<String, AttributeValue>();
        item_values.put("id", new AttributeValue(String.valueOf(trade.getId())));
        item_values.put("tickerSymbol", new AttributeValue(trade.getTickerSymbol()));
        item_values.put("price", new AttributeValue(String.valueOf(trade.getPrice())));

        try {
            System.out.println("Write into DynamoDB...");
            ddb.putItem(table_name, item_values);
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", table_name);
            System.err.println("Be sure that it exists and that you've typed its name correctly.");
            System.exit(1);
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void PutRecord2OutputKinesis(KinesisClient kinesisClient, String streamName,
            StockTrade trade) {

        byte[] bytes = trade.toJsonAsBytes();
        if (bytes == null) {
            System.out.println("Could not get Json bytes for stock trade");
            return;
        }

        System.out.println("Putting trade: " + trade);
        PutRecordRequest request = PutRecordRequest.builder().partitionKey(trade.getTickerSymbol())
                .streamName(streamName).data(SdkBytes.fromByteArray(bytes)).build();

        try {
            kinesisClient.putRecord(request);
        } catch (KinesisException e) {
            e.getMessage();
        }
    }
}
