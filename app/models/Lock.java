package models;

import base.models.ModelBase;
import io.ebean.annotation.CreatedTimestamp;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;


@Entity
public class Lock extends ModelBase {
    @ManyToOne
    public Node node;

    public Date date;

    public String name;
}
