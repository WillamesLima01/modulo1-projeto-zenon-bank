package br.com.zenon.fraud;

public class DBMain {

    public static void main(String[] args) {
        ConnectionFactory.getConnection();
        System.out.println("Conexão com o BD criada!");

        var repository = new TransactionSQLRepository();

        repository.findByOriginName("C1000001")
            .ifPresentOrElse(System.out::println,
                ()-> System.out.println("Transação não encontrada para C1000001"));

        repository.findByOriginName("C100dfgdf0001")
            .ifPresentOrElse(System.out::println,
                ()-> System.out.println("Transação não encontrada para C100dfgdf0001"));



    }

}