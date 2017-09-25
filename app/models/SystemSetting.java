package models;

import base.models.ModelBase;
import io.ebean.Ebean;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


@Entity
public class SystemSetting extends ModelBase {

    public Date ts;

    protected SystemSetting(){}

    private static SystemSetting systemSetting =  null;


    public static SystemSetting findWithCache(){
        if(systemSetting==null){
            systemSetting = findNoCache();
        } else {
            long newts = Ebean.createQuery(SystemSetting.class).select("ts").setMaxRows(1).findOne().ts.getTime();
            if(newts>systemSetting.ts.getTime()){
                systemSetting = findNoCache();
            }
        }
        return systemSetting;
    }

    public static SystemSetting findNoCache(){
        return Ebean.createQuery(SystemSetting.class).select("*").findOne();
    }

    public static SystemSetting find(){
        return findWithCache();
    }



}
