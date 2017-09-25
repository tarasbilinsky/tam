package models;

import base.models.ModelBase;
import io.ebean.annotation.Sql;

import javax.persistence.Entity;


@Entity
@Sql
public class ZipCodeSummary extends ModelBase {
    public String zipCode;
    public Double latitude;
    public Double longitude;
}


