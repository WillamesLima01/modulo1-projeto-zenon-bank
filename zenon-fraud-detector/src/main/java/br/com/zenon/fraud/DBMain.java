package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.util.List;

public class DBMain {

    public static void main(String[] args) {
        ConnectionFactory.getConnection();
        System.out.println("Conexão com o BD criada!");

        var repository = new TransactionSQLRepository();

        var transactionIngestor = new TransactionIngestor();

        long startTimeSQL = System.nanoTime();
        List<Transaction> transactions = transactionIngestor.read("data/PS_20174392719_1491204439457_log.csv");
        System.out.println(transactions.size());
        System.out.println("Iniciando adição das transações no BD...");

        transactions.forEach(repository::save);

        long endTimeSQL = System.nanoTime();
        System.out.println("Tempo de inserção - SQL (ms): " + (endTimeSQL - startTimeSQL) / 1_000_000.0);

        repository.findByOriginName("C1231006815")
       .ifPresentOrElse(System.out::println,
              ()-> System.out.println("Transação não encontrada para C1231006815"));

    }

}