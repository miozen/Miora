package liuyuyang.net.dto.article;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ArticleLikeDTO {
    @ApiModelProperty(value = "本次递增的点赞数", example = "1", required = true)
    @NotNull(message = "点赞数不能为空")
    @Min(value = 1, message = "点赞数至少为 1")
    @Max(value = 100, message = "单次最多点赞 100 次")
    private Integer count;
}
