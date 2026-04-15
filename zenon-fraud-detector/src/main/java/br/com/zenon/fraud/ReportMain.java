package br.com.zenon.fraud;

public class ReportMain {

    public static void main(String[] args) {

       var transactionReport = new TransactionReport();
       var statistics = transactionReport.generateReport("zenon-fraud-detector/data/PS_20174392719_1491204439457_log.csv");
        IO.println("""
                Total de linhas: %d
                Total de fraudes: %d
                Valor total transacionado: %s
                """.formatted(
                statistics.totalTransactions(),
                statistics.totalFrauds(),
                statistics.totalAmount()
        ));

    }
}
