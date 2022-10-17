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
import java.util.ArrayList;
import java.util.HashMap;

public class KinesisHandler implements RequestHandler<KinesisEvent, String> {

    @Override
    public String handleRequest(KinesisEvent kinesisEvent, Context context) {

        String response = "200 OK";
        String table_name = "Ticker";

        HashMap<String, AttributeValue> item_values = new HashMap<String, AttributeValue>();

        item_values.put("id", new AttributeValue("1"));
        item_values.put("tickerSymbol", new AttributeValue("MSFT"));
        item_values.put("price", new AttributeValue("100"));

        final AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.standard().build();

        try {
            ddb.putItem(table_name, item_values);
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", table_name);
            System.err.println("Be sure that it exists and that you've typed its name correctly.");
            System.exit(1);
        } catch (AmazonServiceException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        ObjectMapper JSON = new ObjectMapper();

        for (KinesisEventRecord record : kinesisEvent.getRecords()) {
            // System.out.println(new String(record.getKinesis().getData().array()));
            try {
                StockTrade trade =
                        JSON.readValue(record.getKinesis().getData().array(), StockTrade.class);
                System.out.println(trade.toString());
            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
        }

        return response;
    }
}
