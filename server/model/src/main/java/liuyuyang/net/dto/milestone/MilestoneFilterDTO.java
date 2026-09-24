package liuyuyang.net.dto.milestone;

import io.swagger.annotations.ApiModelProperty;
import liuyuyang.net.dto.FilterDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MilestoneFilterDTO extends FilterDTO {
    @ApiModelProperty(value = "根据标题模糊查询")
    private String title;

    @ApiModelProperty(value = "根据年份精确查询")
    private String year;
}
