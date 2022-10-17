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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;

public class KinesisHandler implements RequestHandler<KinesisEvent, String> {

    @Override
    public String handleRequest(KinesisEvent kinesisEvent, Context context) {

        String response = "200 OK";
        String table_name = "Ticker";

        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.standard().build();
        ObjectMapper JSON = new ObjectMapper();

        for (KinesisEventRecord record : kinesisEvent.getRecords()) {
            try {
                // Deserialize record got from Kinesis
                StockTrade trade =
                        JSON.readValue(record.getKinesis().getData().array(), StockTrade.class);
                System.out.println(trade.toString());

                // DynamoDB
                // Field is applicable only string.
                // Want to change field type for each value's type
                HashMap<String, AttributeValue> item_values = new HashMap<String, AttributeValue>();
                item_values.put("id", new AttributeValue(String.valueOf(trade.getId())));
                item_values.put("tickerSymbol", new AttributeValue(trade.getTickerSymbol()));
                item_values.put("price", new AttributeValue(String.valueOf(trade.getPrice())));

                try {
                    System.out.println("Write into DynamoDB...")
                    ddb.putItem(table_name, item_values);
                } catch (ResourceNotFoundException e) {
                    System.err.format("Error: The table \"%s\" can't be found.\n", table_name);
                    System.err.println(
                            "Be sure that it exists and that you've typed its name correctly.");
                    System.exit(1);
                } catch (AmazonServiceException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
        }

        return response;
    }
}
