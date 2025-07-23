package gr.aueb.cf.system_management_restAPI.core.filters;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Schema(description = "Paginated response wrapper")
public class Paginated<T>{

    @Schema(description = "List of data items")
    List<T> data;

    @Schema(description = "Total number of elements", example = "50")
    long totalElements;

    @Schema(description = "Total number of pages", example = "10")
    int totalPages;

    @Schema(description = "Number of elements in current page", example = "10")
    int numberOfElements;

    @Schema(description = "Current page number (0-based)", example = "0")
    int currentPage;

    @Schema(description = "Page size", example = "10")
    int pageSize;

    public Paginated(Page<T> page) {
        this.data = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.numberOfElements = page.getNumberOfElements();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
    }
}

