package models;

import base.models.ModelBase;
import io.ebean.Ebean;
import io.ebean.annotation.CreatedTimestamp;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.UUID;


@Entity
public class UserSessionIntegration extends ModelBase {
    @ManyToOne
    public User user;

    public String token;

    @CreatedTimestamp
    public Date date;

    public UserSessionIntegration(User user){
        this.user = user;
        token = UUID.randomUUID().toString();
        this.save();
    }

    public static User find(String token){
        UserSessionIntegration i = Ebean.createQuery(UserSessionIntegration.class).select("id").fetch("user").fetch("user.company").where().eq("token",token).findOne();

        if(i!=null){
            User res = i.user;
            i.delete();
            return res;
        } else {
            return null;
        }

    }
}
