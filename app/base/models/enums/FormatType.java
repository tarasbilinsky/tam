package base.models.enums;

import base.types.Money;
import base.utils.Titles;
import models.ZipCode;
import org.bouncycastle.util.IPAddress;

public enum FormatType {
    Undefined,
    Date,
    DateTime,
    Email,
    EmailsList,
    IntegerNumber,
    Number,
    Number4,
    Money,
    ZipCode,
    Password,
    UserName,
    MoneyCanBeNegative,
    Url,
    PhoneNumber,
    IpAddress,

    YesNo,
    BooleanFalseAsDisabled;

    @Override
    public String toString() {
        if(this==Undefined) return ""; else return this.name();
    }
}
