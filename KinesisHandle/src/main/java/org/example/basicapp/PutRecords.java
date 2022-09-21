package org.example.basicapp;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.KinesisException;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;

public class PutRecords {

	public static void main(String[] args) {

		final String usage = "\n" + "Usage:\n" + "   <streamName>\n\n" + "where: \n"
				+ "   streamName - The Amazon Kinesis data stream to which records are written";

		if (args.length != 1) {
			System.out.println(usage);
			System.exit(1);
		}

		String streamName = args[0];
		Region region = Region.AP_NORTHEAST_1;
		KinesisClient kinesisClient = KinesisClient.builder().region(region).build();

		validateStream(kinesisClient, streamName);
		setData(kinesisClient, streamName);
		kinesisClient.close();
	}

	public static void setData(KinesisClient kinesisClient, String streamName) {

		try {
			DataGenerator dataGenerator = new DataGenerator();

			int index = 50;
			for (int x = 0; x < index; x++) {
				StockTrade data = dataGenerator.getRandomData();
				sendData(data, kinesisClient, streamName);
				Thread.sleep(100);
			}
		} catch (KinesisException | InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		System.out.println("Done");
	}

	private static void sendData(StockTrade trade, KinesisClient kinesisClient, String streamName) {
		byte[] bytes = trade.toJsonAsBytes();

		if (bytes == null) {
			System.out.println("Could not get Json bytes for stock trade");
			return;
		}

		System.out.println("Putting trade: " + trade);
		PutRecordRequest request = PutRecordRequest.builder().partitionKey(trade.getTickerSymbol())
				.partitionKey(trade.getTickerSymbol()).streamName(streamName)
				.data(SdkBytes.fromByteArray(bytes)).build();

		try {
			kinesisClient.putRecord(request);
		} catch (KinesisException e) {
			e.getMessage();
		}
	}

	private static void validateStream(KinesisClient kinesisClient, String streamName) {
		try {
			DescribeStreamRequest describeStreamRequest =
					DescribeStreamRequest.builder().streamName(streamName).build();
			DescribeStreamResponse describeStreamResponse =
					kinesisClient.describeStream(describeStreamRequest);

			if (!describeStreamResponse.streamDescription().streamStatus().toString()
					.equals("ACTIVE")) {
				System.err.println("Stream " + streamName
						+ " is not active. Please waita few moments and try again.");
				System.exit(1);
			}
		} catch (KinesisException e) {
			System.err.println("Error found while describing the stream " + streamName);
			System.err.println(e);
			System.exit(1);
		}
	}
}
