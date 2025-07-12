package gr.aueb.cf.system_management_restAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResponseMessageDTO {

    private String message;
    private Boolean success;
    private Object data;

    public ResponseMessageDTO(String message, Boolean success) {
        this.message = message;
        this.success = success;
    }

    public static ResponseMessageDTO success(String message) {
        return new ResponseMessageDTO(message, true);
    }

    public static ResponseMessageDTO success(String message, Object data) {
        return new ResponseMessageDTO(message, true, data);
    }

    public static ResponseMessageDTO error(String message) {
        return new ResponseMessageDTO(message, false);
    }
}