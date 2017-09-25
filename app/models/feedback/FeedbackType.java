package models.feedback;

import base.models.Lookup;
import base.models.ModelBase;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


@Entity
public class FeedbackType extends Lookup {
    @ManyToOne
    public FeedbackArea area;
}
