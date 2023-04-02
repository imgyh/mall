package com.imgyh.mall.auth.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : GithubAccessToken
 * @Package : com.imgyh.mall.auth.vo
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/2 14:02
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GithubAccessToken {
    private String accessToken;

    private String tokenType;

    private String scope;
}
