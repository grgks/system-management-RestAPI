package gr.aueb.cf.system_management_restAPI.core.exceptions;

import org.springframework.validation.BindingResult;

public class ValidationException extends Exception {
    private BindingResult bindingResult;

    public ValidationException(BindingResult bindingResult) {
        super("Validation failed");
        this.bindingResult = bindingResult;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
