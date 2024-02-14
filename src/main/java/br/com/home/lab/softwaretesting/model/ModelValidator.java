package br.com.home.lab.softwaretesting.model;

import javax.validation.*;
import java.util.Set;


public class ModelValidator {

    private ModelValidator(){}

    public static ModelValidator getInstance(){
        return new ModelValidator();
    }

    public <T> Set<ConstraintViolation<T>> validate(final T model){
        Validator validator = createValidator();
        return validator.validate(model);
    }

    protected Validator createValidator() {
        Configuration<?> config = Validation.byDefaultProvider().configure();
        ValidatorFactory factory = config.buildValidatorFactory();
        Validator validator = factory.getValidator();
        factory.close();
        return validator;
    }
}
