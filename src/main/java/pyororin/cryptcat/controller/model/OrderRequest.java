package pyororin.cryptcat.controller.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrderRequest {
    @NotNull
    private String reason;
    @NotNull
    private String group;
    @NotNull
    private int range;
}
