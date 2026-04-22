package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EfficientTransactionIngestor {

    public static final int LINE_BATCH_SIZE = 2_500;

    private final Semaphore dbPermits = new Semaphore(10);

    public void readAsBatch(String fileName, Consumer<List<Transaction>> batchConsumer) {
        Path path = Path.of(fileName);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                Stream<String> lines = Files.lines(path).skip(1)) {

            var iterator = lines.iterator();

            List<String> lineBatch = new ArrayList<>(LINE_BATCH_SIZE);
            while (iterator.hasNext()){
                String line = iterator.next();
                lineBatch.add(line);

                if (lineBatch.size() >= LINE_BATCH_SIZE) {
                    System.out.println("Executando batch ingestor...");
                    List<String> currentLineBatch = List.copyOf(lineBatch);
                    executor.submit(()-> {

                        try {
                            executeBatch(List.copyOf(lineBatch), batchConsumer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        });


                    lineBatch.clear();
                }
            }

            if (!lineBatch.isEmpty()) {
                System.out.println("Executando batch final ingestor...");
                List<String> currentLineBatch = List.copyOf(lineBatch);
                executor.submit(()-> {

                    try {
                        executeBatch(List.copyOf(lineBatch), batchConsumer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler o arquivo: " + fileName, ex);
        }
    }

    private void executeBatch(List<String> lineBatch, Consumer<List<Transaction>> batchConsumer) {
        List<Transaction> transactionBatch = lineBatch
                .stream()
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        try {
            dbPermits.acquire();

            try {
                batchConsumer.accept(transactionBatch);
            } finally {
                dbPermits.release();
            }

        } catch (Exception ex) {

            Thread.currentThread().interrupt();
        }

    }

    public void readAsStream(String fileName, Consumer<Transaction> consumer) {
        Path path = Path.of(fileName);

        try (Stream<String> lines = Files.lines(path)){
                 lines
                .skip(1)
                .map(this::parseTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(consumer);

        } catch (Exception ex) {
            throw new RuntimeException("Erro ao ler o arquivo: " + fileName, ex);
        }
    }

    private Optional<Transaction> parseTransaction(String line) {
        try {
            String[] chunks = line.split(",", -1);

            int step = Integer.parseInt(chunks[0]);
            TransactionType type = TransactionType.valueOf(chunks[1]);

            if (chunks[2] == null || chunks[2].trim().isEmpty()) {
                throw new IllegalArgumentException("O valor de amount não pode ser nulo ou vazio");
            }

            BigDecimal amount = new BigDecimal(chunks[2]);

            var origin = new TransactionCustomer(
                    chunks[3],
                    new BigDecimal(chunks[4]),
                    new BigDecimal(chunks[5])
            );

            var recipient = new TransactionCustomer(
                    chunks[6],
                    new BigDecimal(chunks[7]),
                    new BigDecimal(chunks[8])
            );

            boolean isFraud = "1".equals(chunks[9]);
            boolean isFlaggedFraud = "1".equals(chunks[10]);

            return Optional.of(new Transaction(
                    step,
                    type,
                    amount,
                    origin,
                    recipient,
                    isFraud,
                    isFlaggedFraud
            ));

        } catch (Exception e) {
            System.err.println("Erro ao fazer parse : " + line + " | " + e);
            return Optional.empty();
        }
    }
}