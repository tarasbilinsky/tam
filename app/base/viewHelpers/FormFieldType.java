package base.viewHelpers;

import base.models.ModelBase;

import javax.persistence.Lob;
import java.lang.reflect.Field;

public enum FormFieldType {
    NotDefined,TextInput,TextArea,HtmlEdit,HtmlView,TextView,SelectBox,RadioButtons,Checkboxes,Hidden,TextViewWithHiddenId,PasswordInput;
}
