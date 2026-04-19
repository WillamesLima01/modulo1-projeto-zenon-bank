package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.List;
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
                transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
        System.out.println(transactions.size());
        transactions.stream().limit(10).forEach(System.out::println);

        System.out.println("_______________________________________________________________________________________");

        List<Transaction> transactionsBadData =
                transactionIngestor.read("data/paysim_with_bad_data.csv");
        System.out.println(transactionsBadData.size());
        transactionsBadData.forEach(System.out::println);

        System.out.println("_______________________________________________________________________________________");

        var fraudAnalyzer = new FraudAnalyzer(transactions);

        long fraudCount = fraudAnalyzer.countFrauds();
        System.out.println("Total de fraudes: " + fraudCount);

        List<BigDecimal> highestFraudAmounts = fraudAnalyzer.findHighestValueFraudAmounts(3);

        System.out.println("Top 3 fraudes de maior valor:");

        highestFraudAmounts.forEach(amount -> System.out.printf("- %.2f%n", amount));

        List<String> suspiciousClients = fraudAnalyzer.findTopSuspiciousClients(5);
        System.out.println("Top 5 clientes suspeitos:");
        suspiciousClients.forEach(System.out::println);

        BigDecimal totalFraudLoss = fraudAnalyzer.CalculateTotalFraudLoss();
        System.out.println("Prejuízo total: " + totalFraudLoss);

        Map<TransactionType, Long> fraudCountByType = fraudAnalyzer.countFraudsByType();
        System.out.println("Fraudes por tipo:");
        fraudCountByType.forEach((type, count) -> System.out.println("- %s: %d".formatted(type, count)));

        System.out.println("_______________________________________________________________________________________");

        TransactionRepository transactionRepository;

        transactionRepository = new TransactionListRepository(transactions);
        String notFoundOriginName = "C1868032458";
        transactionRepository.findByOriginName(notFoundOriginName)
                .ifPresentOrElse(System.out::println,
                        () -> System.out.println("Transação não encontrada para o critério informado"));

        String existingOriginName = "C1868032458";

        long startTimeList = System.nanoTime();
        transactionRepository.findByOriginName(existingOriginName)
                .ifPresentOrElse(System.out::println,
                        () -> System.out.println("Transação não encontrada para " + existingOriginName));
        long endTimeList = System.nanoTime();
        System.out.println("Tempo de busca - List (ms): " + ((endTimeList - startTimeList) / 1_000_000.0));

        transactionRepository = new TransactionListRepository(transactions);
        startTimeList = System.nanoTime();
        transactionRepository.findByOriginName(existingOriginName)
                .ifPresentOrElse(System.out::println,
                        () -> System.out.println("Transação não encontrada para " + existingOriginName));
        endTimeList = System.nanoTime();
        System.out.println("Tempo de busca - List (ms): " + ((endTimeList - startTimeList) / 1_000_000.0));
    }
}