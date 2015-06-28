/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.entity;

import java.util.Objects;

/**
 * This entity class represents optimal allocation of the group in terms of
 * minimal price of ticket.
 *
 * @author ids-cd team
 */
public class OptimalGroup {

    int countGroup;
    int countBase;
    int countHalf;
    IdsPrice price;
    int totalGroupPrice;

    public OptimalGroup(int countGroup, int countBase, int countHalf, IdsPrice price, int totalGroupPrice) {
        this.countGroup = countGroup;
        this.countBase = countBase;
        this.countHalf = countHalf;
        this.price = price;
        this.totalGroupPrice = totalGroupPrice;
    }

    public OptimalGroup() {
    }

    public int getCountGroup() {
        return countGroup;
    }

    public void setCountGroup(int countGroup) {
        this.countGroup = countGroup;
    }

    public int getCountBase() {
        return countBase;
    }

    public void setCountBase(int countBase) {
        this.countBase = countBase;
    }

    public int getCountHalf() {
        return countHalf;
    }

    public void setCountHalf(int countHalf) {
        this.countHalf = countHalf;
    }

    public IdsPrice getPrice() {
        return price;
    }

    public void setPrice(IdsPrice price) {
        this.price = price;
    }

    public int getTotalGroupPrice() {
        return totalGroupPrice;
    }

    public void setTotalGroupPrice(int totalGroupPrice) {
        this.totalGroupPrice = totalGroupPrice;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.countGroup;
        hash = 53 * hash + this.countBase;
        hash = 53 * hash + this.countHalf;
        hash = 53 * hash + Objects.hashCode(this.price);
        hash = 53 * hash + this.totalGroupPrice;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OptimalGroup other = (OptimalGroup) obj;
        if (this.countGroup != other.countGroup) {
            return false;
        }
        if (this.countBase != other.countBase) {
            return false;
        }
        if (this.countHalf != other.countHalf) {
            return false;
        }
        if (!Objects.equals(this.price, other.price)) {
            return false;
        }
        if (this.totalGroupPrice != other.totalGroupPrice) {
            return false;
        }
        return true;
    }

}
