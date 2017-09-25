package models;


import base.models.UserRoleBase;
import base.models.annotations.Cached;
import javax.persistence.Entity;


@Entity
public class UserRole extends UserRoleBase {
    public static UserRole get(Long id){
        return UserRole.find(UserRole.class,id);
    }
}
