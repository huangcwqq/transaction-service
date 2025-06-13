package com.hsbc.transaction.repository;

import com.hsbc.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
        * 交易数据访问层接口。
        * 定义了对交易数据进行CRUD操作的方法。
        */
public interface TransactionRepository {

    /**
     * 保存一笔新的交易。
     * @param transaction 要保存的交易对象
     * @return 保存后的交易对象（可能包含生成的ID）
     */
    Transaction save(Transaction transaction);

    /**
     * 根据ID查找交易。
     * @param id 交易ID
     * @return 包含交易的Optional，如果找不到则为空Optional
     */
    Transaction findById(String id);

    /**
     * 获取所有交易。
     * @return 所有交易的列表
     */
    List<Transaction> findAll();

    /**
     * 分页查询所有交易。
     * @param pageable 分页信息
     * @return 包含交易的分页结果
     */
    Page<Transaction> findAll(Pageable pageable);

    /**
     * 更新一笔交易。
     * @param transaction 要更新的交易对象
     * @return 更新后的交易对象
     */
    Transaction update(Transaction transaction);

    /**
     * 根据ID删除交易。
     * @param id 交易ID
     * @return 如果删除成功返回true，否则返回false
     */
    boolean deleteById(String id);

    /**
     * 检查交易ID是否存在。
     * @param id 交易ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(String id);
}
