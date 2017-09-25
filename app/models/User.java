package models;

import base.models.PermissionBase;
import base.models.UserBase;
import base.models.UserRoleBase;
import base.models.annotations.FieldMeta;
import base.models.annotations.FieldMetaOptionsSource;
import base.models.enums.SearchType;
import base.viewHelpers.FormFieldType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User extends UserBase {

    @ManyToOne
    @FieldMeta(serialize = false)
    public UserRole primaryUserRole;

    @ManyToMany
    @FieldMetaOptionsSource()
    public Set<UserRole> roles;
    @ManyToMany
    public Set<UserPermission> permissions;

    @Override
    public Set<Long> getRoles() {
        Set<Long> rr = new HashSet<>();
        for(UserRole r: roles){ rr.add(r.id); }
        return rr;
    }

    @Override
    public Set<Long> getPermissions(){
        Set<Long> rr = new HashSet<>();
        for(UserPermission r: permissions){ rr.add(r.id); }
        return rr;
    }

    @Override
    public Long getPrimaryRole() {
        return primaryUserRole.id;
    }

    public Long idForIntegration;


}
