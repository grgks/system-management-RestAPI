package gr.aueb.cf.system_management_restAPI.core.filters;

import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@Schema(description = "Generic filtering and pagination criteria")
public abstract class GenericFilters {
    private final static int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_SORT_COLUMN = "id";
    private static final Sort.Direction DEFAULT_SORT_DIRECTION = Sort.Direction.ASC;

    @Schema(description = "Page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "10")
    private int pageSize;

    @Schema(description = "Sort direction", example = "ASC", allowableValues = {"ASC", "DESC"})
    private Sort.Direction sortDirection;

    @Schema(description = "Field to sort by", example = "id")
    private String sortBy;

    public int getPageSize() {
        return pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    public int getPage() {
        return Math.max(page, 0);
    }

    public Sort.Direction getSortDirection(){
        if (this.sortDirection == null) return DEFAULT_SORT_DIRECTION;
        return this.sortDirection;
    }

    public String getSortBy(){
        if (this.sortBy == null || StringUtils.isBlank(this.sortBy)) return DEFAULT_SORT_COLUMN;
        return this.sortBy;
    }

    public Pageable getPageable(){
        return PageRequest.of(getPage(), getPageSize(), getSort());
    }

    public Sort getSort(){
        return Sort.by(this.getSortDirection(), this.getSortBy());
    }
}
