package com.bank.transaction.repository;

import com.bank.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * TransactionRepository的内存实现。
 * 使用ConcurrentHashMap来模拟内存中的数据存储，确保线程安全。
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    // 使用ConcurrentHashMap存储交易，键是交易ID，值是Transaction对象。
    // ConcurrentHashMap是线程安全的，适合高并发场景下的内存数据存储。
    private final ConcurrentHashMap<String, Transaction> transactions = new ConcurrentHashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        // 由于是内存存储，这里直接将交易放入map中。
        // 在实际应用中，如果ID是数据库生成的，会在这里设置。
        // 对于本作业，ID在Service层生成。
        transactions.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Transaction findById(String id) {
        // 从map中根据ID获取交易,不存在时返回 null。
        return transactions.get(id);
    }

    @Override
    public List<Transaction> findAll() {
        // 返回所有交易的列表。
        // 注意：ConcurrentHashMap的values()方法返回的是一个Collection视图，
        // 这里为了避免外部修改对内部数据结构造成影响，转换为新的ArrayList。
        return new ArrayList<>(transactions.values());
    }

    @Override
    public Page<Transaction> findAll(Pageable pageable) {
        // 对于内存存储，我们首先获取所有交易，然后进行内存分页。
        // 对于大数据集，这种方式效率低下，通常会使用持久化数据库。
        List<Transaction> allTransactions = transactions.values().stream()
                // 默认按日期降序排序，可以根据实际需求调整
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTransactions.size());

        // 确保子列表索引不越界
        List<Transaction> pagedTransactions;
        if (start < allTransactions.size()) {
            pagedTransactions = allTransactions.subList(start, end);
        } else {
            pagedTransactions = new ArrayList<>(); // 如果起始索引超出列表大小，返回空列表
        }

        return new PageImpl<>(pagedTransactions, pageable, allTransactions.size());
    }

    @Override
    public Transaction update(Transaction transaction) {
        // 更新交易：如果ID存在，则替换旧的交易对象。
        if (transactions.containsKey(transaction.getId())) {
            transactions.put(transaction.getId(), transaction);
            return transaction;
        }
        // 如果不存在，根据业务逻辑可能抛出异常或返回null。
        // 在本例中，Service层会先检查是否存在。
        return null; // 通常在Service层处理“未找到”的情况
    }

    @Override
    public boolean deleteById(String id) {
        // 根据ID删除交易。
        // remove方法会返回被移除的值，如果值不存在则返回null。
        return transactions.remove(id) != null;
    }

    @Override
    public boolean existsById(String id) {
        // 检查交易ID是否存在。
        return transactions.containsKey(id);
    }
}
