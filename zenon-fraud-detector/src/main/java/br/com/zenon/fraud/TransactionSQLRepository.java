package br.com.zenon.fraud;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TransactionSQLRepository implements TransactionRepository {

    @Override
    public void save(Transaction transaction) {
        String sql = """
                INSERT INTO transactions
                (step, `type`, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient,
                 old_balance_recipient, new_balance_recipient, is_fraud, is_flagged_fraud)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transaction.step());
            ps.setString(2, transaction.type().name());
            ps.setBigDecimal(3, transaction.amount());

            ps.setString(4, transaction.origin().name());
            ps.setBigDecimal(5, transaction.origin().oldBalance());
            ps.setBigDecimal(6, transaction.origin().newBalance());

            ps.setString(7, transaction.recipient().name());
            ps.setBigDecimal(8, transaction.recipient().oldBalance());
            ps.setBigDecimal(9, transaction.recipient().newBalance());

            ps.setBoolean(10, transaction.isFraud());
            ps.setBoolean(11, transaction.isFlaggedFraud());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar transação no banco.", e);
        }
    }

    @Override
    public Optional<Transaction> findByOriginName(String originName) {
        String sql = """
                SELECT id, step, type, amount, name_origin, old_balance_origin, new_balance_origin, name_recipient,
                       old_balance_recipient, new_balance_recipient, is_fraud, is_flagged_fraud
                FROM transactions
                WHERE name_origin = ?
                ORDER BY step
                LIMIT 1
                """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, originName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Transaction transaction = mapResultSetToTransaction(rs);
                    return Optional.of(transaction);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar transação por originName.", e);
        }

        return Optional.empty();
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) {
        try {
            int step = rs.getInt("step");
            TransactionType type = TransactionType.valueOf(rs.getString("type"));
            BigDecimal amount = rs.getBigDecimal("amount");

            String originName = rs.getString("name_origin");
            BigDecimal originOldBalance = rs.getBigDecimal("old_balance_origin");
            BigDecimal originNewBalance = rs.getBigDecimal("new_balance_origin");
            TransactionCustomer origin = new TransactionCustomer(originName, originOldBalance, originNewBalance);

            String recipientName = rs.getString("name_recipient");
            BigDecimal recipientOldBalance = rs.getBigDecimal("old_balance_recipient");
            BigDecimal recipientNewBalance = rs.getBigDecimal("new_balance_recipient");
            TransactionCustomer recipient = new TransactionCustomer(recipientName, recipientOldBalance, recipientNewBalance);

            boolean isFraud = rs.getBoolean("is_fraud");
            boolean isFlaggedFraud = rs.getBoolean("is_flagged_fraud");

            return new Transaction(step, type, amount, origin, recipient, isFraud, isFlaggedFraud);

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao mapear ResultSet para Transaction.", e);
        }
    }
}