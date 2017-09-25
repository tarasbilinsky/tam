package models.feedback;

import base.models.ModelBase;
import io.ebean.annotation.CreatedTimestamp;
import models.User;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.util.Date;


@Entity
public class Feedback extends ModelBase {
    public Feedback(){}

    public Feedback(Date dateUser, String url, String descriptionShort, String description) {
        this.dateUser = dateUser;
        this.url = url;
        this.descriptionShort = descriptionShort;
        this.description = description;
    }

    @CreatedTimestamp
    public Date date;

    public Date dateUser;

    @ManyToOne
    public User user;

    public String url;

    @ManyToOne
    public FeedbackArea area;

    @ManyToOne
    public FeedbackType feedbackType;

    public String descriptionShort;

    @Lob
    public String description;

    public String sessionId;
}
