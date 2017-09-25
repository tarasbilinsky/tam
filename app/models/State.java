package models;

import base.models.ModelBase;

import javax.persistence.Entity;


@Entity
public class State extends ModelBase {
    public String name;
    public String shortName;
    public String toString(){
        return name;
    }
}
