package models;

import base.models.ModelBase;
import io.ebean.annotation.CreatedTimestamp;

import javax.persistence.Entity;
import javax.persistence.Lob;
import java.util.Date;


@Entity
public class Log extends ModelBase {
    @CreatedTimestamp
    public Date date;

    public String tag;
    @Lob
    public String message;

    public Log(String tag, String message) {
        this.tag = tag;
        this.message = message;
        this.save();
    }
}

