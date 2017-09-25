package models;

import base.models.ModelBase;
import io.ebean.Ebean;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


@Entity
public class ZipCode extends ModelBase {

    public String zipCode;
    public String city;

    @ManyToOne
    public State state;
    public Double latitude;
    public Double longitude;

    @Override
    public String toString(){ return zipCode;}

    public static ZipCode parse(String zip){
        if(zip==null) return null;
        zip = zip.trim();
        ZipCode zipCode = Ebean.createQuery(ZipCode.class).select("zipCode,latitude,longitude").fetch("state","id").where().eq("zipCode", zip).findOne();
        if(zipCode==null){
            if(zip==null || zip.length()<5){
                return null;
            }
            ZipCodeSummary zipCodeSp;
            String sql;
            try {
                sql = String.format("select zip_code,latitude,longitude, abs(cast(substring(zip_code,3,2) as SIGNED)-%d) closest_number " +
                        "from zip_code " +
                        "where substring(zip_code,1,3)='%s' " +
                        "order by closest_number " +
                        "limit 1", Integer.parseInt(zip.substring(3, 5)), zip.substring(0, 3));
            } catch (NumberFormatException e){
                return null;
            }
            RawSql rawSql = RawSqlBuilder.unparsed(sql)
                    .columnMapping("zip_code", "zipCode")
                    .columnMapping("latitude", "latitude")
                    .columnMapping("longitude", "longitude")
                    .columnMappingIgnore("closest_number")
                    .create();
            zipCodeSp = Ebean.createQuery(ZipCodeSummary.class).setRawSql(rawSql).findOne();
            if(zipCodeSp==null){
                return null;
            }
            zipCode = Ebean.createQuery(ZipCode.class).select("zipCode,latitude,longitude").fetch("state","id").where().eq("zipCode", zipCodeSp.zipCode).findOne();
        }
        return zipCode;
    }
}