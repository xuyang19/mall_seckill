package com.nowcoder.mall.dao;

import com.nowcoder.mall.entity.ItemStockLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface ItemStockLogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock_log
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock_log
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    int insert(ItemStockLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock_log
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    ItemStockLog selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock_log
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    List<ItemStockLog> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock_log
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    int updateByPrimaryKey(ItemStockLog record);
}