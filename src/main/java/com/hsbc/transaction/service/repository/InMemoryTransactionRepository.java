package com.hsbc.transaction.service.repository;

import com.hsbc.transaction.service.model.Transaction;
import com.hsbc.transaction.service.util.IdGenerateUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransactionRepository的内存实现。
 * 使用ConcurrentHashMap来模拟内存中的数据存储，确保线程安全。
 * @Repository 注解表示这是一个Spring管理的仓储组件。
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
    public Optional<Transaction> findById(String id) {
        // 从map中根据ID获取交易，使用Optional包装结果，避免空指针。
        return Optional.ofNullable(transactions.get(id));
    }

    @Override
    public List<Transaction> findAll() {
        // 返回所有交易的列表。
        // 注意：ConcurrentHashMap的values()方法返回的是一个Collection视图，
        // 这里为了避免外部修改对内部数据结构造成影响，转换为新的ArrayList。
        return new ArrayList<>(transactions.values());
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
