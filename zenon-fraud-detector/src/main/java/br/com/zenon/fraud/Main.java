package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        var t1 = new Transaction(
                1,
                TransactionType.PAYMENT,
                new BigDecimal("9839.64"),
                new TransactionCustomer("C1231006815", new BigDecimal("170136.0"), new BigDecimal("160296.36")),
                new TransactionCustomer("M1979787155", new BigDecimal("0.0"), new BigDecimal("0.0")),
                false,
                false
        );

        var t2 = new Transaction(
                743,
                TransactionType.CASH_OUT,
                new BigDecimal("850002.52"),
                new TransactionCustomer("C1280323807", new BigDecimal("850002.52"), new BigDecimal("0.0")),
                new TransactionCustomer("C873221189", new BigDecimal("6510099.11"), new BigDecimal("7360101.63")),
                true,
                false
        );

        System.out.println(t1);
        System.out.println(t2);

        System.out.println("____________________________________________");

        var transactionIngestor = new TransactionIngestor();

        List<Transaction> transactions =
                transactionIngestor.read("zenon-fraud-detector/data/PS_20174392719_1491204439457_log.csv");
        System.out.println(transactions.size());
        transactions.stream().limit(10).forEach(System.out::println);

        System.out.println("_______________________________________________________________________________________");

        List<Transaction> transactionsBadData =
                transactionIngestor.read("zenon-fraud-detector/data/paysim_with_bad_data.csv");
        System.out.println(transactionsBadData.size());
        transactionsBadData.forEach(System.out::println);

        System.out.println("_______________________________________________________________________________________");

        var fraudAnalyzer = new FraudAnalyzer(transactions);

        long fraudCount = fraudAnalyzer.countFrauds();
        IO.println("Total de fraudes: " + fraudCount);

        List<BigDecimal> highestFraudAmounts = fraudAnalyzer.findHighestValueFraudAmounts(3);

        IO.println("Top 3 fraudes de maior valor:");

        highestFraudAmounts.stream()
                .forEach(amount -> System.out.printf("- %.2f%n", amount));

        List<String> suspiciousClients = fraudAnalyzer.findTopSuspiciousClients(5);
        IO.println("Top 5 clientes suspeitos:");
        suspiciousClients.forEach(IO::println);

        BigDecimal totalFraudLoss = fraudAnalyzer.CalculateTotalFraudLoss();
        IO.println("Prejuízo total: " + totalFraudLoss);

        Map<TransactionType, Long> fraudCountByType = fraudAnalyzer.countFraudsByType();
        IO.println("Fraudes por tipo:");
        fraudCountByType.forEach((type, count) -> IO.println("- %s: %d".formatted(type, count)));
    }

}