package com.ym.materials.optimize.fastRemoveList;

/**
 * Created by ym on 2018/4/29.
 */
public class Vo {

    private String name;

    public Vo(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vo vo = (Vo) o;

        return name != null ? name.equals(vo.name) : vo.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Vo{" +
                "name='" + name + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
