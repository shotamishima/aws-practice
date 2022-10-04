package org.example.lambdabasic;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;

public class KinesisHandler implements RequestHandler<KinesisEvent, Void> {

    @Override
    public Void handleRequest(KinesisEvent kinesisEvent, Context context) {
        for (KinesisEventRecord record : kinesisEvent.getRecords()) {
            System.out.println(new String(record.getKinesis().getData().array()));

        }

        return null;
    }
}
