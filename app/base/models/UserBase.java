package base.models;

import base.models.annotations.FieldFormat;
import base.models.annotations.FieldMeta;
import base.models.annotations.FieldMetaFormat;
import base.models.enums.FormatType;

import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import java.util.HashSet;
import java.util.Set;


@MappedSuperclass
public abstract class UserBase extends ModelBase {
    @FieldMeta(title = "User Name")
    public String name;

    public String password;

    @FieldMeta(serialize = false)
    @FieldFormat(value = FormatType.BooleanFalseAsDisabled, param="inactive")
    public boolean active = true;

    @FieldFormat(FormatType.EmailsList)
    @FieldMeta(title = "Email")
    public String email;

    @FieldMeta(title = "Phone")
    public String phone;

    public Set<Long> getRoles(){return new HashSet<>();}

    public Set<Long> getPermissions(){return new HashSet<>();}

    abstract public Long getPrimaryRole();

}
