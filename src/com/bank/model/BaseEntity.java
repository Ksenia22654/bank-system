package com.bank.model;

import java.io.Serializable;

/** Базовый класс всех сущностей: идентификатор, сравнение и строковое представление. */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == 0;
    }

    /** Запись уже сохранена в базе. Для JSP: слово new в выражениях EL использовать нельзя. */
    public boolean isPersisted() {
        return id != 0;
    }

    /** Краткое название записи для отображения пользователю. */
    public abstract String getTitle();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return id != 0 && id == ((BaseEntity) other).id;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() * 31 + id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + id + " (" + getTitle() + ")";
    }
}
