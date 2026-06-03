package com.importorder.dao;

import com.importorder.model.ReceivedGoods;

import java.util.List;
import java.util.Optional;

public interface ReceivedGoodsDAO {

    List<ReceivedGoods> findAll();

    Optional<ReceivedGoods> findById(long id);

    Optional<ReceivedGoods> findBySiteOrderItemId(long siteOrderItemId);

    ReceivedGoods save(ReceivedGoods receivedGoods);

    void update(ReceivedGoods receivedGoods);

    void deleteById(long id);
}
