package com.bank.dao;

import com.bank.model.BaseEntity;

import java.util.List;

/** Обобщённый контракт объекта доступа к данным (паттерн DAO). */
public interface Dao<T extends BaseEntity> {
    T findById(int id);

    List<T> findAll();

    T save(T entity);

    boolean delete(int id);
}
