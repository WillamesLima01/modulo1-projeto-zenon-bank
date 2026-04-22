package br.com.zenon.fraud;

public class IngestionMain {

    public static void main(String[] args) {

        var repository = new TransactionSQLRepository();

        var transactionIngestor = new EfficientTransactionIngestor();

        long startTimeSQL = System.nanoTime();
        transactionIngestor.readAsBatch("data/PS_20174392719_1491204439457_log.csv", repository::saveAll);

        long endTimeSQL = System.nanoTime();
        System.out.println("Tempo de ingestão no DB (ms): " + (endTimeSQL - startTimeSQL) / 1_000_000.0);

    }
}
