package com.nowcoder.mall.entity;

public class ItemStockLog {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column item_stock_log.id
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column item_stock_log.item_id
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    private Integer itemId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column item_stock_log.amount
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    private Integer amount;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column item_stock_log.status
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    private Integer status;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column item_stock_log.id
     *
     * @return the value of item_stock_log.id
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column item_stock_log.id
     *
     * @param id the value for item_stock_log.id
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column item_stock_log.item_id
     *
     * @return the value of item_stock_log.item_id
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column item_stock_log.item_id
     *
     * @param itemId the value for item_stock_log.item_id
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column item_stock_log.amount
     *
     * @return the value of item_stock_log.amount
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column item_stock_log.amount
     *
     * @param amount the value for item_stock_log.amount
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column item_stock_log.status
     *
     * @return the value of item_stock_log.status
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column item_stock_log.status
     *
     * @param status the value for item_stock_log.status
     *
     * @mbg.generated Sat Dec 11 16:20:41 CST 2021
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ItemStockLog{" +
                "id='" + id + '\'' +
                ", itemId=" + itemId +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}